package com.example.myfirstapp.background;

import static com.example.myfirstapp.app.CHANNEL_1_ID;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.myfirstapp.app.CHANNEL_1_ID;

public class DryerWorker extends Worker
{
    private AxesModel savedAxes = new AxesModel();

    //For Notifications
    private NotificationManagerCompat notificationManager;

    public DryerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams)
    {
        super(context, workerParams);
        //Notification
        notificationManager = NotificationManagerCompat.from(getApplicationContext());
    }

    @NonNull
    @Override
    public Result doWork()
    {

        //Read Saved Range from file and save it to savedAxes
        readFromFile();

        //Set as important
        setForegroundAsync(createInfo());

        //Call connect to client
        int errorNumber = connectToClient();
        System.out.println("Work Error Number: " + errorNumber);

        Data dryerOutput = new Data.Builder()
                .putInt("dryerOutput", errorNumber)
                .build();


        if (errorNumber == 0)
        {

            return Result.success(dryerOutput);
        }
        else
        {
            return Result.failure(dryerOutput);
        }

    }


    int connectToClient()
    {

        //Setup Json String
        String json = "{\"axisX\":" + savedAxes.getAxisX() + ",\n" +
                        "\"axisY\":" + savedAxes.getAxisY() + ",\n" +
                        "\"axisZ\":" + savedAxes.getAxisZ() +  "}";
        System.out.println(json);

        //Model that holds the information to pull data from the Client.
        ClientModel piModel = new ClientModel();

        //Needed to call the Pi
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);


        System.out.println("START OF THE CLICK--------------------------------------");

        //Setup OkHttpClient
        OkHttpClient client = new OkHttpClient();

        //Setup the builder so that it does not time out after 10 seconds (Needed for dryer
        //I believe write is the part that needs to wait awhile.
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(1, TimeUnit.MINUTES) // connect timeout
                .writeTimeout(2, TimeUnit.HOURS) // write timeout
                .readTimeout(2, TimeUnit.HOURS); // read timeout

        client = builder.build();

        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        //IT BETTER WORK
        RequestBody body = RequestBody.Companion.create(json, JSON);

        //Setup the request and pick the URL we are calling
        Request postRequest = new Request.Builder()
                .post(body)
                .url("http://" + piModel.getIpAddress() + ":" + piModel.getPort() + "/DryPi/dryStop")  //api that is being called
                .addHeader("cache-control", "no-cache")
                .addHeader("Authorization", "Bearer " + piModel.getToken()) //Security Token
                .build();
        try {
            System.out.println("START OF END--------------------------------------");

            //Execute call.
            Response response = client.newCall(postRequest).execute();

            //Take the response and save it to a string.
            int statusCode = response.code();
            System.out.println(statusCode);
            System.out.println(response.body());

            System.out.println("END OF THE CLICK--------------------------------------");

            //If the API returns a bad response then assume error
            if (statusCode != 200)
            {
                return 1;
            }

            //If response is not adjust then assume an error.
            return 0;

        }
        catch (IOException e)
        {
            System.out.println("Bad CLICK---------------------------------");
            e.printStackTrace();
            return 2;
        }
    }

    /**
     * Take Calibrated range from the Raspberry Pi and save it to a text file to use it later.
     * Currently reads the same text file to show it has been created. That will be moved to Dryer Fragment later
     *
     */
    private void readFromFile()
    {

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
            System.out.println(line);
            //Convert the range to an axesModel
            savedAxes.setAxisX((Double.parseDouble(line)));
            line = reader.readLine();
            savedAxes.setAxisY((Double.parseDouble(line)));
            line = reader.readLine();
            savedAxes.setAxisZ((Double.parseDouble(line)));

            String contents = stringBuilder.toString();
            System.out.println("File Found:" + savedAxes.getAxisX() + " " + savedAxes.getAxisY() + " " + savedAxes.getAxisZ());
        }
        catch (IOException e)
        {
            System.out.println(e);

        }

    }

    /**
     * Will be used to send notification when fully implemented Currently left behind from prototype.
     */
    private ForegroundInfo createInfo()
    {
        String title = "Dryer Reminder";
        String message = "Your Dryer detection has started!";
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_one)
                .setContentTitle(title)
                .setContentText(message)
                .build();

        return new ForegroundInfo(1, notification);

    }

    /*
    private ForegroundInfo createForegroundInfo(@NonNull String progress) {
        // Build a notification using bytesRead and contentLength

        Context context = getApplicationContext();
        String id = context.getString(R.string.notification_channel_id);
        String title = context.getString(R.string.notification_title);
        String cancel = context.getString(R.string.cancel_download);
        // This PendingIntent can be used to cancel the worker
        PendingIntent intent = WorkManager.getInstance(context)
                .createCancelPendingIntent(getId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }

        Notification notification = new NotificationCompat.Builder(context, id)
                .setContentTitle(title)
                .setTicker(title)
                .setSmallIcon(R.drawable.ic_work_notification)
                .setOngoing(true)
                // Add the cancel action to the notification which can
                // be used to cancel the worker
                .addAction(android.R.drawable.ic_delete, cancel, intent)
                .build();

        return new ForegroundInfo(notification);
    }

     */

}
