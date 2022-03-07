package com.example.myfirstapp.background;

import static com.example.myfirstapp.app.CHANNEL_1_ID;

import android.app.Notification;
import android.content.Context;
import android.os.StrictMode;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myfirstapp.R;
import com.example.myfirstapp.model.AxesModel;
import com.example.myfirstapp.model.ClientModel;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Worker to be used by the Dryer Fragment. This will send the saved range from calibrate to ditirmine when the dryer stops.
 * If the API returns the same data that was sent it then the dryer has stopped.
 * This must be ran as a Foreground asynchronous task, since the call to the Pi may last more then 10 minutes.
 */
public class DryerWorker extends Worker
{

    /**
     * Constructor for the worker class
     * @param context Important details about the user's phone needed to run certain methods
     * @param workerParams Details about the worker
     */
    public DryerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams)
    {
        super(context, workerParams);
        Log.i("Dryer Worker", "Created");
    }

    /**
     * The method that will be used when the worker is called from the fragment.
     * @return Result which returns a success or failure, along with output relating to why.
     */
    @NonNull
    @Override
    public Result doWork()
    {

        Log.i("Dryer Worker", "Worker Started");

        //Read Saved Range from file and save it to savedAxes
        AxesModel savedAxes = readFromFile();

        //If the values are not default then the file was read correctly
        if(savedAxes.getAxisX() != 0 && savedAxes.getAxisY() != 0 && savedAxes.getAxisZ() != 0)
        {
            //This will send a notification to the user to let them know it is running as an important task
            //Without this the worker would shut down in less then 10 minutes
            setForegroundAsync(createInfo());

            //Call connect to API
            int errorNumber = connectToApi(savedAxes);
            //int errorNumber = 0; //For testing app with no pi

            Log.i("Dryer Worker", "Error Number is - " + errorNumber);

            //Setup data to send the errorNumber.
            Data dryerOutput = new Data.Builder()
                    .putInt("dryerOutput", errorNumber)
                    .build();

            //If error number is zero, return success, else return failure
            if (errorNumber == 0)
            {
                Log.i("Dryer Worker", "Worker is a Success");
                return Result.success(dryerOutput);
            }
            else
            {
                Log.e("Dryer Worker", "Worker has Failed From Connection Problems");

                //Cancels the Notify worker since dryer might not have stopped.
                WorkManager.getInstance(getApplicationContext()).cancelAllWorkByTag("Notify");
                return Result.failure(dryerOutput);
            }
        }
        else
        {
            Log.e("Dryer Worker", "Worker has Failed From File Problems");

            //Setup data to send the errorNumber.
            Data dryerOutput = new Data.Builder()
                    .putInt("dryerOutput", 2)
                    .build();

            //Cancels the Notify worker since dryer might not have stopped.
            WorkManager.getInstance(getApplicationContext()).cancelAllWorkByTag("Notify");
            return Result.failure(dryerOutput);
        }
    }


    /**
     * Uses OkHttp to POST a JSON value as request to the Raspberry Pi's API. The value will be the saved range from Calibrate
     * A response code is used to determine if the dryer has stopped or an error has happened.
     * @return int which determines the result of attempting to connect to API.
     */
    private int connectToApi(AxesModel savedAxes)
    {
        Log.i("Dryer Worker API", "Method has started");

        //Setup Json String
        String json = "{\"axisX\":" + savedAxes.getAxisX() + ",\n" +
                        "\"axisY\":" + savedAxes.getAxisY() + ",\n" +
                        "\"axisZ\":" + savedAxes.getAxisZ() +  "}";

        Log.i("Dryer Worker API", "JSON Setup as " + json);

        //Model that holds the information to pull data from the API.
        ClientModel piModel = new ClientModel();

        //Needed to call the Pi
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);


        //Setup OkHttpClient
        OkHttpClient client = new OkHttpClient();

        //Setup the builder so that it does not time out after 10 seconds (Needed for dryer
        //The Write/Read Connection is open for a long time since a dryer could take up to two hours to finish.
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(1, TimeUnit.MINUTES) // connect timeout
                .writeTimeout(3, TimeUnit.HOURS) // write timeout
                .readTimeout(3, TimeUnit.HOURS); // read timeout
        client = builder.build();

        //Makes the attribute of the request a JSON request
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.Companion.create(json, JSON);

        //Setup the request and pick the URL
        Request postRequest = new Request.Builder()
                .post(body)
                .url("http://" + piModel.getIpAddress() + ":" + piModel.getPort() + "/DryPi/dryStop")  //api that is being called
                .addHeader("cache-control", "no-cache")
                .addHeader("Authorization", "Bearer " + piModel.getToken()) //Security Token
                .build();

        Log.i("Dryer Worker API", "OkHttpClient has been built");
        Log.i("Dryer Worker API", "IpAddress - " + piModel.getIpAddress() + " Port - " + piModel.getPort());


        //Attempt to connect to API
        try
        {
            Log.i("Dryer Worker API", "Attempting to connect to API");

            //Execute call.
            Response response = client.newCall(postRequest).execute();

            //Take the response and save it to a string.
            int statusCode = response.code();

            Log.i("Dryer Worker API", "Response status is " + statusCode);
            Log.i("Dryer Worker API", "Response body is " + response.body());

            //If the API returns a bad response then assume error
            if (statusCode != 200)
            {
                //Return 1 as a response error
                Log.e("Dryer Worker API", "Connection was not successful");
                return 1;
            }

            //Return a 0 as success
            Log.i("Dryer Worker API", "Connection was successful");
            return 0;

        }
        catch (IOException e)
        {
            Log.w("Dryer Worker API", "Connection failed");
            Log.e("Dryer Worker API", e.toString());

            //Return 2 as an exception error.
            return 2;
        }
    }

    /**
     * Find calibrated range from a text file that was saved from the calibrate worker.
     * Will save it to the savedRange variable.
     *
     */
    private AxesModel readFromFile()
    {
        Log.i("Dryer Worker Read", "Method has started");

        AxesModel savedAxes = new AxesModel();

        //Create the file input stream
        FileInputStream fileInput = null;

        //Try to find text file
        try
        {
            Log.i("Dryer Worker Read", "Attempt to find file");
            fileInput = getApplicationContext().openFileInput("configCalibrate.txt"); //Use context to read from text file

            //Take the text file and try to convert it to an AxesModel
            InputStreamReader inputStreamReader = new InputStreamReader(fileInput, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(inputStreamReader))
            {
                Log.i("Dryer Worker Read", "Attempt to read from file");

                String line = reader.readLine();

                //Convert the range to an axesModel
                savedAxes.setAxisX((Double.parseDouble(line)));
                line = reader.readLine();
                savedAxes.setAxisY((Double.parseDouble(line)));
                line = reader.readLine();
                savedAxes.setAxisZ((Double.parseDouble(line)));

                String contents = stringBuilder.toString();

                Log.i("Dryer Worker Read", "Saved Range is X: " + savedAxes.getAxisX() + ", Y: " + savedAxes.getAxisY() + ", Z: " + savedAxes.getAxisZ());

            }
            catch (IOException e)
            {
                Log.w("Dryer Worker Read", "Reading file failed");
                Log.e("Dryer Worker Read", e.toString());

            }

        }
        catch (FileNotFoundException e)
        {
            Log.w("Dryer Worker Read", "Finding file failed");
            Log.e("Dryer Worker Read", e.toString());
        }

        return savedAxes;

    }

    /**
     * A notification needs to be created to inform the user that their phone is
     * running a task for a long period of time.
     */
    private ForegroundInfo createInfo()
    {
        Log.i("Dryer Worker Info", "Method has started");

        String title = "Dryer Reminder";
        String message = "Your Dryer detection has started!";
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_one)
                .setContentTitle(title)
                .setContentText(message)
                .build();

        Log.i("Dryer Worker Info", "ForegroundInfo Created");

        return new ForegroundInfo(1, notification);

    }


}
