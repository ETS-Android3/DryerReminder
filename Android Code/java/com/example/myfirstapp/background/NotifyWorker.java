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

import java.util.TimerTask;

public class NotifyWorker extends Worker
{

    //For Notifications
    private NotificationManagerCompat notificationManager;
    private boolean firstCall = true;

    public NotifyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams)
    {
        super(context, workerParams);


        //Notification
        notificationManager = NotificationManagerCompat.from(getApplicationContext());

    }

    @NonNull
    @Override
    public Result doWork()
    {

        System.out.println("Notify Worker");
        //TimerTask have to be setup this way. This will send a notification every few minutes
        TimerTask task = new TimerTask()
        {

            //Will be run every x amount of time specified
            @Override
            public void run()
            {
                sendOnChannel1(firstCall);

                firstCall = false;
            }
        };


        //Calls the schedule to run ever specified amount. Likely based on ticks. 1000 ticks is 1 second for reference.
        DryerFragment.watch.schedule(task, 200, minutesToTicks(1));


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
}


