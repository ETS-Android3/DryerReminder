package com.example.myfirstapp.presenter;

import android.os.StrictMode;

import com.example.myfirstapp.model.AxesModel;
import com.example.myfirstapp.model.ClientModel;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;



public class CalibratePresenter implements CalibrateContract.Presenter {
    public CalibrateContract.View view;
    private AxesModel savedAxes;

    public CalibratePresenter(CalibrateContract.View view)
    {
        this.view = view;
    }

    //Call the pi and get the axes models. Save it to text file
    public void doCalibrate()
    {
        ClientModel piModel = new ClientModel();

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            //Above needed for get to work
            System.out.println("START OF THE CLICK--------------------------------------");
            //Setup OkHttpClient
            OkHttpClient client = new OkHttpClient();

            //Setup the builder so that it does not time out after 10 seconds (Needed for dryer
            //I believe write is the part that needs to wait awhile.
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(2, TimeUnit.HOURS) // connect timeout
                    .writeTimeout(2, TimeUnit.HOURS) // write timeout
                    .readTimeout(2, TimeUnit.HOURS); // read timeout

            client = builder.build();

            //For empty post
            RequestBody formBody = new FormBody.Builder()
                    .build();

            //Setup the request and pick the URL we are calling
            Request postRequest = new Request.Builder()
                    .post(formBody)
                    .url("http://" + piModel.getIpAddress() + ":" + piModel.getPort()+"/DryPi/calibrate")  //api that is being called
                    .addHeader("cache-control", "no-cache")
                    .addHeader("Authorization", "Bearer " + piModel.getToken())
                    .build();
            try {
                System.out.println("START OF END--------------------------------------");
                Response response = client.newCall(postRequest).execute();
                System.out.println(response.body().string());

                System.out.println("END OF THE CLICK--------------------------------------");
            } catch (IOException e) {
                System.out.println("Start OF THE CLICK BAD---------------------------------");
                e.printStackTrace();

            }
        }
    }

}
