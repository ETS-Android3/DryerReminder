package com.example.myfirstapp.background;

import static android.content.Context.VIBRATOR_SERVICE;
import static com.example.myfirstapp.app.CHANNEL_1_ID;

import android.app.Notification;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myfirstapp.DryerFragment;
import com.example.myfirstapp.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Worker to be used by the Dryer Fragment after the dryer worker. This will send a notification to tell the user the
 * dryer stopped. Then by using the time saved from Notify fragment, it will remind the user constantly until they stop it.
 * This MAY need to be ran as a Foreground asynchronous task. More testing needed
 */
public class NotifyWorker extends Worker
{

    //To Manage Notifications
    private NotificationManagerCompat notificationManager;
    private boolean firstCall = true; //Checks if the device has been called
    //Initialize a Vibrate Function
    private Vibrator vibrator;


    /**
     * Constructor for the worker class
     * @param context Important details about the user's phone needed to run certain methods
     * @param workerParams Details about the worker
     */
    public NotifyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams)
    {
        super(context, workerParams);

        //Notification
        notificationManager = NotificationManagerCompat.from(getApplicationContext());

        //Vibrate Function
        vibrator = (Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE);

        Log.i("Notify Worker", "Worker Created");

    }

    /**
     * The method that will be used when the worker is called from the fragment.
     * @return Result which returns a success or failure, along with output relating to why.
     */
    @NonNull
    @Override
    public Result doWork()
    {

        Log.i("Notify Worker", "Worker Started");

        sendOnChannel1(true);
        vibrator.vibrate(500); //Vibrate
        Log.i("Notify Worker", "Sent First Notification");





        //TimerTask has to be setup this way. This will send a notification every few minutes
        TimerTask task = new TimerTask()
        {

            //Will be run every x amount of time specified
            @Override
            public void run()
            {

                if(!firstCall)
                {
                    sendOnChannel1(false);
                    vibrator.vibrate(500); //Vibrate
                    Log.i("Notify Worker", "Sent Reminder Notification");
                }
                firstCall = false;
            }
        };


        //Find how many minutes based on text file
        String foundText = readFromFile();
        int minutes = decideMinutes(foundText);


        Log.i("Notify Worker", minutes + " Minutes Between Each Notification");

        //If minutes equal or less then zero ignore timer task.
        if (minutes > 0)
        {
            Log.i("Notify Worker", "Notification will Reoccurring");

            //Calls the schedule to run ever specified amount. Likely based on ticks. 1000 ticks is 1 second for reference.
            DryerFragment.watch = new Timer();
            DryerFragment.watch.schedule(task, 200, minutesToTicks(minutes));
        }


        //Return success since the task is now being run. Will need to cancel work when button is press again.
        Log.i("Notify Worker", "Worker is a Success");
        return Result.success();
    }

    /**
     * Send notification to inform user the dryer has stopped.
     * @param firstCalled Determines the message used in the notification bar.
     */

    public void sendOnChannel1(boolean firstCalled)
    {

        String message;

        Log.i("Notify Worker Channel", "Work is called for the first time?: " + firstCalled);


        //If first call tell user dryer is finished. If any other tell them not to forget their laundry.
        if (firstCalled)
        {
            message = "Your dryer has finished!";
        }
        else
        {
            message = "Don't forget your laundry!";
        }
        

        String title = "Dryer Reminder";
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_one)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)

                .build();


        notificationManager.notify(1, notification);
    }

    /**
     * Converts ticks to minutes to make it easier to under how often the notification will go off.
     * @param x How many minutes the user wants the notification to go off.
     * @return The amount of ticks in minutes
     */
    private long minutesToTicks(int x)
    {
        Log.i("Notify Worker Ticks", "Method Called");
        //1000 ticks is a second. 60 seconds is a minute.
        return x * 60 * 1000;
    }

    /**
     * Reads text file saved from notify fragment and
     * returns string of it's content. Used to determine
     * how often to send notification, if at all.
     *
     * @return String of file's content
     */
    public String readFromFile()
    {

        Log.i("Notify Worker Read", "Method has started");

        //Create the file input stream
        FileInputStream fileInput = null;

        //Try to find text file
        try
        {
            fileInput = getApplicationContext().openFileInput("configNotify.txt"); //Use context to read from text file

            //Take the text file and try to read from it
            InputStreamReader inputStreamReader = new InputStreamReader(fileInput, StandardCharsets.UTF_8);
            try (BufferedReader reader = new BufferedReader(inputStreamReader))
            {
                Log.i("Notify Worker Read", "Attempt to read from file");

                //Save file line as to a string variable
                String line = reader.readLine();
                String textRead = line;

                Log.i("Notify Worker Read", "Notify text found is " + textRead);

                return textRead;
            }
            catch (IOException e)
            {
                Log.w("Notify Worker Read", "Reading file failed");
                Log.e("Notify Worker Read", e.toString());


            }
        }
        catch (FileNotFoundException e)
        {
            Log.w("Notify Worker Read", "Finding file failed");
            Log.e("Notify Worker Read", e.toString());
        }

        //If error is caught then assume the user does not want this feature
        return "Never";
    }

    /**
     * Translates string from notify into a number, in the form of minutes.
     * Defaults to 0 if file is not found.
     * @param textFound String found from text file
     * @return Int that represents minutes between notifications.
     */
    private int decideMinutes(String textFound)
    {
        Log.i("Notify Worker Minutes", "Method Started");

        int minutes = 0;

        if(textFound.equals("Never"))
        {
            minutes = 0;
        }
        else if(textFound.equals("1 Minute"))
        {
            minutes = 1;
        }
        else if(textFound.equals("5 Minutes"))
        {
            minutes = 5;
        }
        else if(textFound.equals("10 Minutes"))
        {
            minutes = 10;
        }
        else if(textFound.equals("15 Minutes"))
        {
            minutes = 15;
        }

        Log.i("Notify Worker Minutes", "There are " + minutes + " minutes");
        return minutes;
    }
}


