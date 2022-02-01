package com.example.myfirstapp.presenter;

import android.content.Context;
import android.os.StrictMode;
import android.widget.Toast;

import com.example.myfirstapp.model.AxesModel;
import com.example.myfirstapp.model.ClientModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;



/**
 * Presenter for the Calibrate Fragment class. Acts like a controller to the Fragment's View.
 * Calls the API from the Raspberry Pi to get calibration data from the accelerometer.
 */
public class CalibratePresenter implements CalibrateContract.Presenter
{
    //Variables
    public CalibrateContract.View view;
    private AxesModel savedAxes = new AxesModel();

    /**
     * Allows the view to be called in the presenter.
     * To call methods in the fragment that need context
     *
     * @param view Calibrate Fragment
     */
    public CalibratePresenter(CalibrateContract.View view)
    {
        this.view = view;

    }

    /**
     * Method that calls another method to call the API. Currently
     * separated because the piCall will likely be moved to background tasks.
     *
     */
    public int doCalibrate()
    {
        System.out.println("Presenter works");

        return piCall();


    }

    /**
     * Void method that calls the Calibrate API on the Pi and receive a JSON of the AxesModel.
     *
     */
    private int piCall()
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
        builder.connectTimeout(2, TimeUnit.MINUTES) // connect timeout
                .writeTimeout(2, TimeUnit.MINUTES) // write timeout
                .readTimeout(2, TimeUnit.MINUTES); // read timeout

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
        try {
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
            view.writeToFile(savedAxes);


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



}
