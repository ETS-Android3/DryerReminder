package com.example.myfirstapp.background;

import android.content.Context;
import android.os.StrictMode;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myfirstapp.model.AxesModel;
import com.example.myfirstapp.model.ClientModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdjustWorker extends Worker
{
    private AxesModel savedAxes = new AxesModel();
    int adjustNumber;

    public AdjustWorker(@NonNull Context context, @NonNull WorkerParameters workerParams)
    {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork()
    {

        //Pull number from Adjust Fragment
        adjustNumber = getInputData().getInt("adjustSeekbar", 3);
        System.out.println(adjustNumber);

        //Call connect to client
        int errorNumber = connectToClient();
        System.out.println("Work Error Number: " + errorNumber);

        Data adjustOutput = new Data.Builder()
                .putInt("adjustOutput", errorNumber)
                .build();

        if (errorNumber == 0)
        {
            return Result.success(adjustOutput);
        }
        else
        {
            return Result.failure(adjustOutput);
        }

    }


    int connectToClient()
    {

        //Setup Json String
        String json = "{\"adjust\":" + adjustNumber + "}";
        System.out.print(json);

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

        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        //IT BETTER WORK
        RequestBody body = RequestBody.Companion.create(json, JSON);

        //Setup the request and pick the URL we are calling
        Request postRequest = new Request.Builder()
                .post(body)
                .url("http://" + piModel.getIpAddress() + ":" + piModel.getPort()+"/DryPi/adjust")  //api that is being called
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
            System.out.println(jsonString);

            System.out.println("END OF THE CLICK--------------------------------------");

            //If the API does not return Adjust, return 0
            if(jsonString.equals("Adjust"))
            {
                return 0;
            }

            //If response is not adjust then assume an error.
            return 1;
        }
        catch (IOException e)
        {
            System.out.println("Bad CLICK---------------------------------");
            e.printStackTrace();
            return 2;
        }
    }
}
