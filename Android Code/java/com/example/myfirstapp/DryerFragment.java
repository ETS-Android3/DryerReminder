package com.example.myfirstapp;

import android.app.Notification;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.navigation.fragment.NavHostFragment;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myfirstapp.background.DryerWorker;
import com.example.myfirstapp.background.NotifyWorker;
import com.example.myfirstapp.databinding.FragmentDryerBinding;
import com.example.myfirstapp.model.TimerModel;

import java.util.List;



/**
 * Dryer Fragment allows user to call the Pi to get a notification when the Pi
 * detects no motion.
 *
 */
public class DryerFragment extends Fragment
{

    private FragmentDryerBinding binding;

    //For Notifications
    private NotificationManagerCompat notificationManager;

    private TextView dryerText;
    private Button dryerButton;
    private Button backButton;

    //Call Worker and Live Data with Worker Info
    private WorkManager myClientManager;
    private LiveData<List<WorkInfo>> mySavedWorkerInfo;

    private boolean firstCall; //Checks if the device has been called
    private boolean firstNotifyCall;
    private boolean dryerStatusOff;

    /**
     * Setup the Dryer page with the layout, view, view group, etc.
     *
     * @param inflater converts the XML file into a view
     * @param container Creates the layout design for the view and View Group
     * @param savedInstanceState Holds the data for mapping values like strings
     * @return binding Binding class used with the Dryer XML file
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        //Set to true when user first comes to page
        firstCall = true;
        firstNotifyCall = true;
        dryerStatusOff = true;


        //Initialize WorkerManager and WorkerInfo with tag Adjust
        myClientManager = WorkManager.getInstance(getActivity().getApplicationContext());
        mySavedWorkerInfo = myClientManager.getWorkInfosByTagLiveData("Dryer");

        binding = FragmentDryerBinding.inflate(inflater, container, false);
        dryerButton = binding.getRoot().findViewById(R.id.dryer_confirm_button);
        backButton = binding.getRoot().findViewById(R.id.dryer_back_button);
        dryerText = binding.getRoot().findViewById(R.id.DryerView);
        return binding.getRoot();

    }

    /**
     * Create all the actions for the view.
     *
     * @param view Creates the Dryer user interface
     * @param savedInstanceState Holds the data for mapping values like strings
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        //Click Listener for Count
        //FOR USE FOR THE GET WITH A STRING!!!!!!!!!!!!!!!!!!!!!
        view.findViewById(R.id.dryer_confirm_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                //Sets status of dryer. If on start the detection process. If off then either cancel the worker or the constant notifications
                if (dryerStatusOff == true)
                {
                    //Set to false so device can show in progress to user
                    firstCall = false;
                    dryerStatusOff = false;


                    //Attempt to connect to Pi and save the adjust value
                    try
                    {

                        //If the version of Android is too old, then prevent the button from running.
                        int SDK_INT = android.os.Build.VERSION.SDK_INT;
                        if (SDK_INT > 8)
                        {
                            System.out.println("----------------------------Start");


                            //Setup work request using the Dryer worker and tag it under Dryer.
                            OneTimeWorkRequest DryerWorker = new OneTimeWorkRequest.Builder(DryerWorker.class)
                                    .addTag("Dryer")
                                    .build();


                            //Start the adjust background task
                            WorkContinuation start = myClientManager.beginUniqueWork("DryingReminder", ExistingWorkPolicy.KEEP, DryerWorker);
                            start.enqueue();




                            System.out.println("----------------------------END");
                        }
                        else
                        {
                            //Create and show a toast that says the device's SDK is out of date
                            Toast myToast = Toast.makeText(getActivity(), "Outdated Version. Cannot connect to device", Toast.LENGTH_LONG);
                            myToast.show();
                        }
                    }
                    catch (Exception e)
                    {
                        //If exception is caught show failure to user
                        System.out.println(e);
                        showFailure();
                    }
                    // put log here
                }
                else
                {
                    dryerStatusOff = true;

                    //Turn off notification. Should add cancel for workers too.
                    TimerModel.watch.cancel();

                    //Change button text
                    dryerButton.setText("Start");
                }

            }
        });

        //Navigate User to Setting Fragment by clicking the Back Button
        binding.dryerBackButton.setOnClickListener(viewBack ->
        {




            //Specify the navigation action
            NavHostFragment.findNavController(DryerFragment.this)
                    .navigate(R.id.action_dryerFragment_to_HomeFragment);
        });



    //Will show the status when the background task is called
        mySavedWorkerInfo.observe(getViewLifecycleOwner(), listOfWorkInfos -> {

        //If work is null or empty then do nothing.
        if (listOfWorkInfos == null || listOfWorkInfos.isEmpty())
        {
            return;
        }

        //The Tagged LiveData will get called here
        WorkInfo workInfo = listOfWorkInfos.get(0);

        //LiveData persists after first called. This keeps it from updating until getting called first.
        if (!firstCall)
        {
            //Check if background task is finished.
            boolean finished = workInfo.getState().isFinished();
            System.out.println(finished);
            if (!finished)
            {
                //Change texts and buttons to show adjust is in progress
                showInProgress();

            }
            else
            {

                //Pull number from clients output
                int errorNumber = workInfo.getOutputData().getInt("dryerOutput", 3);
                System.out.println(errorNumber);

                //Check if the client returned properly
                if(errorNumber == 0)
                {
                    //Prevent this from being called multiple times by using a boolean.
                    if(firstNotifyCall)
                    {
                        firstNotifyCall = false;

                        //Change text and buttons to show Adjust has finished.
                        showFinishedProgress();
                        System.out.println("Finished process");

                        //Setup work request using the Notify worker and tag it under Notify.
                        OneTimeWorkRequest NotifyWorker = new OneTimeWorkRequest.Builder(com.example.myfirstapp.background.NotifyWorker.class)
                                .addTag("Notify")
                                .build();

                        //Start the adjust background task
                        WorkContinuation start = myClientManager.beginUniqueWork("NotifyReminder", ExistingWorkPolicy.KEEP, NotifyWorker);
                        start.enqueue();

                    }

                }
                else if (errorNumber == 1)
                {
                    firstCall = true;
                    //Change texts and buttons to show adjust failed
                    showFailure();

                }
                else if (errorNumber == 2)
                {
                    firstCall = true;
                    //Change texts and buttons to show adjust failed
                    showFailure();
                }
            }

            //Clear work info list to reset data if button is pressed again
            listOfWorkInfos.clear();
        }


    });
}

    /**
     * Destroys the view by setting the binding to null.
     */
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }


    /**
     * Visually change the buttons, text, and anything else on the screen
     * to show that the dryer has stopped spinning.
     */
    public void showFinishedProgress()
    {
        //Text Changes
        dryerText.setText("Your Dryer Has Stopped!");

        //Dryer Button Changes
        dryerButton.setText("I got my Laundry");
        dryerButton.setBackgroundColor(getResources().getColor(R.color.green_grey));

        //Back Button Changes
        backButton.setBackgroundColor(getResources().getColor(R.color.green_grey));
        backButton.setEnabled(true);
    }

    /**
     * Visually change the buttons, text, and anything else on the screen
     * to show that the phone is waiting on word from the dryer reminder.
     */
    public void showInProgress()
    {
        //Text Changes
        dryerText.setText("Dryer is still spinning");


        //Dryer Button Changes
        dryerButton.setText("I got my Laundry");
        dryerButton.setBackgroundColor(getResources().getColor(R.color.yellow_grey));


        //Back Button Changes
        backButton.setBackgroundColor(getResources().getColor(R.color.yellow_grey));
        backButton.setEnabled(false);
    }

    /**
     * Visually change the buttons, text, and anything else on the screen
     * to show that the phone failed to finish it's process.
     */
    public void showFailure()
    {
        dryerText.setText("Dryer Detection failed");

        //Dryer Button Changes
        dryerButton.setText("Start");
        dryerButton.setBackgroundColor(getResources().getColor(R.color.red_grey));

        //Back Button Changes
        backButton.setBackgroundColor(getResources().getColor(R.color.red_grey));
        backButton.setEnabled(true);
    }


}

