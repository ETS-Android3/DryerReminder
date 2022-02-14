package com.example.myfirstapp.background;

import android.content.Context;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myfirstapp.model.AxesModel;
import com.example.myfirstapp.model.ClientModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Client worker that will
 */
public class ClientWorker extends Worker
{

    private AxesModel savedAxes = new AxesModel();


    public ClientWorker(@NonNull Context context, @NonNull WorkerParameters workerParams)
    {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork()
    {
        if (connectToClient() == 0)
        {
            return Result.success();
        }
        else
        {
            return Result.failure();
        }

    }


    int connectToClient()
    {
        //Model that holds the information to pull data from the Client.
        ClientModel piModel = new ClientModel();

        //Needed to call the Pi
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);


        System.out.println("START OF THE CLICK--------------------------------------");

        //Setup OkHttpClient
        OkHttpClient client = new OkHttpClient();

        //Determines how long the client will try to connect to the URL before timing out.
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(1, TimeUnit.MINUTES) // connect timeout
                .writeTimeout(1, TimeUnit.MINUTES) // write timeout
                .readTimeout(1, TimeUnit.MINUTES); // read timeout

        client = builder.build();

        //Needed to post, even from empty form
        RequestBody formBody = new FormBody.Builder()
                .build();

        //Setup the request and pick the URL we are calling
        Request postRequest = new Request.Builder()
                .post(formBody)
                .url("http://" + piModel.getIpAddress() + ":" + piModel.getPort()+"/DryPi/calibrate")  //api that is being called
                .addHeader("cache-control", "no-cache")
                .addHeader("Authorization", "Bearer " + piModel.getToken()) //Security Token
                .build();
        try
        {
            System.out.println("START OF END--------------------------------------");

            //Execute call.
            Response response = client.newCall(postRequest).execute();

            //Take the response and save it to a string.
            String jsonString = response.body().string();
            //System.out.println(jsonString);

            //Convert the string to a JSON
            JSONObject json = new JSONObject(jsonString);

            //Convert JSON Object to the AxesModel
            savedAxes = new AxesModel(json.getDouble("axisX"), json.getDouble("axisY"), json.getDouble("axisZ"));

            //Call the view to save the axes to a text file.
            writeToFile(savedAxes);


            System.out.println("END OF THE CLICK--------------------------------------");
            return 0;
        }
        catch (IOException | JSONException e)
        {
            System.out.println("Bad CLICK---------------------------------");
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * Take Calibrated range from the Raspberry Pi and save it to a text file to use it later.
     * Currently reads the same text file to show it has been created. That will be moved to Dryer Fragment later
     * @param savedRange Save range that was calibrated from pi
     *
     */
    public void writeToFile(AxesModel savedRange)
    {
        System.out.println("Write to File-----------------------");
        //Try to write to a text file
        try
        {
            //Write saved range to config Calibrate text file
            String filename = "configCalibrate.txt";
            String fileContents = (savedRange.getAxisX() + "\n" + savedRange.getAxisY() + "\n" + savedRange.getAxisZ() + "\n");

            try (FileOutputStream fileOutput = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE))
            {
                fileOutput.write(fileContents.getBytes(StandardCharsets.UTF_8));
            }

        }
        catch(IOException e)
        {
            System.out.println(e);
        }

        //Reading the file Move to Dryer Later------------------------------
        //Converts text file numbers to axes model
        AxesModel newRange = new AxesModel();

        //Create the file input stream
        FileInputStream fileInput = null;

        //Try to find text file
        try
        {
            fileInput = getApplicationContext().openFileInput("configCalibrate.txt"); //Use context to read from text file
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        //Take the text file and try to convert it to an AxesModel
        InputStreamReader inputStreamReader = new InputStreamReader(fileInput, StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(inputStreamReader))
        {
            String line = reader.readLine();

            //Convert the range to an axesModel
            newRange.setAxisX((Double.parseDouble(line)));
            line = reader.readLine();
            newRange.setAxisY((Double.parseDouble(line)));
            line = reader.readLine();
            newRange.setAxisZ((Double.parseDouble(line)));

            String contents = stringBuilder.toString();
            System.out.println("File Found:" + newRange.getAxisX() + " " + newRange.getAxisY() + " " + newRange.getAxisZ());
        }
        catch (IOException e)
        {
            System.out.println(e);
        }

    }
}
