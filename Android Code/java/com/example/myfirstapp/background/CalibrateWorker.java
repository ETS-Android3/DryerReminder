package com.example.myfirstapp.background;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myfirstapp.model.AxesModel;
import com.example.myfirstapp.model.ClientModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Worker to be used by the Calibrate Fragment. This will call the Pi and tell it to calibrate the device.
 * The API will then return that data from calibration. After it will be saved to a text file on the phone.
 * This will run in the background on the phone.
 */
public class CalibrateWorker extends Worker
{

    //Axes of the saved calibrated ranges
    private AxesModel savedAxes = new AxesModel();


    /**
     * Constructor for the worker class
     * @param context Important details about the user's phone needed to run certain methods
     * @param workerParams Details about the worker
     */
    public CalibrateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams)
    {
        super(context, workerParams);
        Log.i("Calibrate Worker", "Worker Created");
    }

    /**
     * The method that will be used when the worker is called from the fragment.
     * @return Result which returns a success or failure, along with output relating to why
     */
    @NonNull
    @Override
    public Result doWork()
    {

        Log.i("Calibrate Worker", "Worker Started");

        //Call connect to API and save error number
        int errorNumber = connectToClient();
        Log.i("Calibrate Worker", "Error Number is - " + errorNumber);

        //Setup data to send the errorNumber.
        Data calibrateOutput = new Data.Builder()
                .putInt("calibrateOutput", errorNumber)
                .build();


        //If error number is zero, return success, else return failure
        if (errorNumber == 0)
        {
            Log.i("Calibrate Worker", "Worker is a Success");
            return Result.success(calibrateOutput);
        }
        else
        {
            Log.e("Calibrate Worker", "Worker has Failed From Connection Problems");
            return Result.failure(calibrateOutput);
        }

    }


    /**
     * Uses OkHttp to POST an empty request to the Raspberry Pi's API.
     * It will  receive a JSON value of the saved range that will need to be saved.
     *
     * @return int which determines the result of attempting to connect to API.
     */
    int connectToClient()
    {
        Log.i("Calibrate Worker API", "Method has started");

        //Model that holds the information to pull data from the Client.
        ClientModel piModel = new ClientModel();

        //Needed to call the Pi
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);


        //Setup OkHttpClient
        OkHttpClient client = new OkHttpClient();

        //Determines how long the client will try to connect to the URL before timing out.
        //Calibrate should be quick so time is short
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(1, TimeUnit.MINUTES) // connect timeout
                .writeTimeout(1, TimeUnit.MINUTES) // write timeout
                .readTimeout(1, TimeUnit.MINUTES); // read timeout
        client = builder.build();

        //Needed to post, even from empty form
        RequestBody formBody = new FormBody.Builder()
                .build();

        //Setup the request and pick the URL
        Request postRequest = new Request.Builder()
                .post(formBody)
                .url("http://" + piModel.getIpAddress() + ":" + piModel.getPort()+"/DryPi/calibrate")  //api that is being called
                .addHeader("cache-control", "no-cache")
                .addHeader("Authorization", "Bearer " + piModel.getToken()) //Security Token
                .build();

        Log.i("Calibrate Worker API", "OkHttpClient has been built");
        Log.i("Calibrate Worker API", "IpAddress - " + piModel.getIpAddress() + " Port - " + piModel.getPort());


        //Attempt to connect to API.
        try
        {
            Log.i("Calibrate Worker API", "Attempting to connect to API");

            //Execute call.
            Response response = client.newCall(postRequest).execute();

            //Take the response and save it to a string.
            String jsonString = response.body().string();

            //Convert the string to a JSON
            JSONObject json = new JSONObject(jsonString);

            //Convert JSON Object to the AxesModel
            savedAxes = new AxesModel(json.getDouble("axisX"), json.getDouble("axisY"), json.getDouble("axisZ"));

            Log.i("Calibrate Worker API", "Response body is " + response.body());
            Log.i("Calibrate Worker API", "Axes Model is X: " + savedAxes.getAxisX() + ", Y: " + savedAxes.getAxisY() + ",Z: " + savedAxes.getAxisZ());

            //Call the view to save the axes to a text file.
            writeToFile(savedAxes);



            //Return 0 meaning the API call and file written succeeded.
            Log.i("Calibrate Worker API", "Connection was successful");
            return 0;
        }
        catch (IOException | JSONException e)
        {
            Log.w("Calibrate Worker API", "Connection failed");
            Log.e("Calibrate Worker API", e.toString());

            //Return a 1 meaning an error happened.
            return 1;
        }
    }

    /**
     * Take Calibrated range from the Raspberry Pi and save it to a text file to use it later.
     * @param savedRange Save range that was calibrated from pi
     *
     */
    public void writeToFile(AxesModel savedRange)
    {
        Log.i("Calibrate Worker Write", "Method has started");

        //Try to write to a text file
        try
        {
            Log.i("Calibrate Worker Write", "Trying to Write File");
            //Write saved range to config Calibrate text file
            String filename = "configCalibrate.txt";
            String fileContents = (savedRange.getAxisX() + "\n" + savedRange.getAxisY() + "\n" + savedRange.getAxisZ() + "\n");

            //Attempt to save to text file using phones context
            try (FileOutputStream fileOutput = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE))
            {
                fileOutput.write(fileContents.getBytes(StandardCharsets.UTF_8));
            }

            Log.i("Calibrate Worker Write", "File Written");
        }
        catch(IOException e)
        {
            Log.w("Calibrate Worker API", "Writing Failed");
            Log.e("Calibrate Worker API", e.toString());
        }

    }
}
