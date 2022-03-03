package com.example.myfirstapp.background;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myfirstapp.model.ClientModel;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Worker to be used by the Adjust Fragment. This will post a JSON value to the Raspberry Pi to determine the sensitivity of the device.
 * This will run in the background on the phone.
 */
public class AdjustWorker extends Worker
{
    //Number used to determine sensitivity.
    int adjustNumber;

    /**
     * Constructor for the worker class
     * @param context Important details about the user's phone needed to run certain methods
     * @param workerParams Details about the worker
     */
    public AdjustWorker(@NonNull Context context, @NonNull WorkerParameters workerParams)
    {
        super(context, workerParams);
        Log.i("Adjust Worker", "Created");
    }

    /**
     * The method that will be used when the worker is called from the fragment.
     * @return Result which returns a success or failure, along with output relating to why
     */
    @NonNull
    @Override
    public Result doWork()
    {
        Log.i("Adjust Worker", "Worker Started");

        //Pull number from Adjust Fragment
        adjustNumber = getInputData().getInt("adjustSeekbar", 3);
        Log.i("Adjust Worker", "Adjust Number is - " + adjustNumber);

        //Call connect to API and save error number
        int errorNumber = connectToApi();
        Log.i("Adjust Worker", "Error Number is - " + errorNumber);

        //Setup data to send the errorNumber.
        Data adjustOutput = new Data.Builder()
                .putInt("adjustOutput", errorNumber)
                .build();

        //If error number is zero, return success, else return failure
        if (errorNumber == 0)
        {
            Log.i("Adjust Worker", "Worker is a Success");
            return Result.success(adjustOutput);
        }
        else
        {
            Log.e("Adjust Worker", "Worker has Failed");
            return Result.failure(adjustOutput);
        }

    }


    /**
     * Uses OkHttp to POST a JSON value as request to the Raspberry Pi's API. The value will determine the value of the sensitivity.
     * It will also receive a JSON value that decides if the request worked.
     * @return int which determines the result of attempting to connect to API.
     */
    private int connectToApi()
    {
        Log.i("Adjust Worker API", "Method has started");

        //Setup the Json String
        String json = "{\"adjust\":" + adjustNumber + "}";
        Log.i("Adjust Worker API", "JSON Setup as " + adjustNumber);

        //Model that holds the information to pull data from the API.
        ClientModel piModel = new ClientModel();

        //Needed to call the Pi
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);


        //Setup OkHttpClient
        OkHttpClient client = new OkHttpClient();

        //Determines how long the client will try to connect to the URL before timing out.
        //Adjust should be quick so time is short
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(1, TimeUnit.MINUTES) // connect timeout
                .writeTimeout(1, TimeUnit.MINUTES) // write timeout
                .readTimeout(1, TimeUnit.MINUTES); // read timeout
        client = builder.build();

        //Makes the attribute of the request a JSON request
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.Companion.create(json, JSON);

        //Setup the request and pick the URL
        Request postRequest = new Request.Builder()
                .post(body)
                .url("http://" + piModel.getIpAddress() + ":" + piModel.getPort()+"/DryPi/adjust")  //api that is being called
                .addHeader("cache-control", "no-cache")
                .addHeader("Authorization", "Bearer " + piModel.getToken()) //Security Token
                .build();

        Log.i("Adjust Worker API", "OkHttpClient has been built");
        Log.i("Adjust Worker API", "IpAddress - " + piModel.getIpAddress() + " Port - " + piModel.getPort());

        //Attempt to connect to API.
        try
        {
            Log.i("Adjust Worker API", "Attempting to connect to API");

            //Execute call.
            Response response = client.newCall(postRequest).execute();

            //Take the response and save it to a string.
            String jsonString = response.body().string();
            Log.i("Adjust Worker API", "Response body is " + jsonString);


            //If the API returns Adjust, return 0 meaning it succeed.
            if(jsonString.equals("Adjust"))
            {
                Log.i("Adjust Worker API", "Connection was successful");
                return 0;
            }

            //If response is not adjust then assume an error.
            return 1;
        }
        catch (IOException e)
        {

            Log.i("Adjust Worker API", "Connection failed");
            Log.e("Adjust Worker API", e.toString());

            //Return 2 as an exception error.
            return 2;
        }
    }
}
