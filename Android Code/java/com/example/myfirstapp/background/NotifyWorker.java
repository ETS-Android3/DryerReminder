package com.example.myfirstapp.background;

import static com.example.myfirstapp.app.CHANNEL_1_ID;

import android.app.Notification;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myfirstapp.DryerFragment;
import com.example.myfirstapp.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

    }

    /**
     * The method that will be used when the worker is called from the fragment.
     * @return Result which returns a success or failure, along with output relating to why.
     */
    @NonNull
    @Override
    public Result doWork()
    {

        System.out.println("Notify Worker");

        //Send a notification telling the user their dryer stopped.
        sendOnChannel1(true);

        //TimerTask has to be setup this way. This will send a notification every few minutes
        TimerTask task = new TimerTask()
        {

            //Will be run every x amount of time specified
            @Override
            public void run()
            {
                sendOnChannel1(false);

            }
        };


        //Find how many minutes based on text file
        String foundText = readFromFile();

        int minutes = decideMinutes(foundText);


        //If minutes equal or less then zero ignore timer task.
        if (minutes > 0)
        {
            //Calls the schedule to run ever specified amount. Likely based on ticks. 1000 ticks is 1 second for reference.
            DryerFragment.watch = new Timer();
            DryerFragment.watch.schedule(task, 200, minutesToTicks(minutes));
        }


        //Return success since the task is now being run. Will need to cancel work when button is press again.
        return Result.success();
    }

    /**
     * Send notification to inform user the dryer has stopped.
     * @param firstCall Determines the message used in the notification bar.
     */
    public void sendOnChannel1(boolean firstCall)
    {
        String message;

        //If first call tell user dryer is finished. If any other tell them not to forget their laundry.
        if (firstCall)
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

        //Create the file input stream
        FileInputStream fileInput = null;

        //Try to find text file
        try
        {
            fileInput = getApplicationContext().openFileInput("configNotify.txt"); //Use context to read from text file
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        //Take the text file and try to read from it
        InputStreamReader inputStreamReader = new InputStreamReader(fileInput, StandardCharsets.UTF_8);
        try (BufferedReader reader = new BufferedReader(inputStreamReader))
        {
            //Save file line as to a string variable
            String line = reader.readLine();
            String textRead = line;

            System.out.println("File Found:" + textRead);

            return textRead;
        }
        catch (IOException e)
        {
            System.out.println(e);
            //If error is caught then assume the user does not want this feature
            return "Never";

        }
    }

    /**
     * Translates string from notify into a number, in the form of minutes.
     * Defaults to 0 if file is not found.
     * @param textFound String found from text file
     * @return Int that represents minutes between notifications.
     */
    private int decideMinutes(String textFound)
    {
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

        return minutes;
    }
}


