package com.example.myfirstapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.navigation.fragment.NavHostFragment;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myfirstapp.background.DryerWorker;
import com.example.myfirstapp.databinding.FragmentDryerBinding;

import java.util.List;
import java.util.Timer;


/**
 * Dryer Fragment allows user to call the Pi to get a notification when the Pi
 * detects no motion. Will also call the Notify worker to find out how often to
 * remind user dryer has stopped.
 *
 */
public class DryerFragment extends Fragment
{

    //For Notifications
    private NotificationManagerCompat notificationManager;

    //Variables based on front-end items
    private FragmentDryerBinding binding;
    private TextView dryerText;
    private Button dryerButton;
    private Button backButton;

    //Call Worker and Live Data with Worker Info
    private WorkManager myClientManager;
    private LiveData<List<WorkInfo>> mySavedWorkerInfo;

    //Booleans for deciding the status of the page
    private boolean firstCall; //Checks if the device has been called
    private boolean firstNotifyCall;
    private boolean dryerStatusOff;

    //Timer to call and cancel for notification. Needs to be public.
    public static Timer watch = new Timer();

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
        Log.i("Dryer Fragment", "Created");

        //Set to true when user first comes to page
        firstCall = true;
        firstNotifyCall = true;
        dryerStatusOff = true;


        //Initialize WorkerManager and WorkerInfo with tag Adjust
        myClientManager = WorkManager.getInstance(getActivity().getApplicationContext());
        mySavedWorkerInfo = myClientManager.getWorkInfosByTagLiveData("Dryer");

        //Binding Fragment and front end items
        binding = FragmentDryerBinding.inflate(inflater, container, false);
        dryerButton = binding.getRoot().findViewById(R.id.dryer_confirm_button);
        backButton = binding.getRoot().findViewById(R.id.dryer_back_button);
        dryerText = binding.getRoot().findViewById(R.id.DryerView);
        return binding.getRoot();

    }

    /**
     * Create all the actions for the Dryer view.
     *
     * @param view Creates the Dryer user interface
     * @param savedInstanceState Holds the data for mapping values like strings
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        //Listens for a button click for the Dryer confirm button
        view.findViewById(R.id.dryer_confirm_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                Log.i("Dryer Fragment Confirm", "Confirm Clicked");

                //Sets status of dryer. If on start the detection process. If off then either cancel the worker or the constant notifications
                if (dryerStatusOff == true)
                {
                    Log.i("Dryer Fragment Confirm", "Dryer status was off");

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
                            Log.i("Dryer Fragment Confirm", "SDK is Above 8");


                            //Setup work request using the Dryer worker and tag it under Dryer.
                            OneTimeWorkRequest DryerWorker = new OneTimeWorkRequest.Builder(DryerWorker.class)
                                    .addTag("Dryer")
                                    .build();

                            //Setup work request using the Notify worker and tag it under Notify.
                            OneTimeWorkRequest NotifyWorker = new OneTimeWorkRequest.Builder(com.example.myfirstapp.background.NotifyWorker.class)
                                    .addTag("Notify")
                                    .build();

                            //Start the Dryer background task, then notify
                            WorkContinuation start = myClientManager.beginUniqueWork("DryingReminder", ExistingWorkPolicy.KEEP, DryerWorker).then(NotifyWorker);
                            start.enqueue();

                            Log.i("Dryer Fragment Confirm", "Dryer and Notify Workers Built And Started");

                        }
                        else
                        {
                            //Create and show a toast that says the device's SDK is out of date
                            Log.i("Dryer Fragment Confirm", "SDK is 8 or Below");
                            Toast myToast = Toast.makeText(getActivity(), "Outdated Version. Cannot connect to device", Toast.LENGTH_LONG);
                            myToast.show();
                        }
                    }
                    catch (Exception e)
                    {
                        //If exception is caught show failure to user
                        showFailure();
                        Log.w("Dryer Fragment Confirm", "Worker Not Started");
                        Log.e("Dryer Fragment Confirm", e.toString());
                    }

                    Log.i("Dryer Fragment Confirm", "Confirm Finished");
                }
                else
                {
                    Log.i("Dryer Fragment Confirm", "Dryer status was On");
                    dryerStatusOff = true;

                    //Turn off notification. Should add cancel for workers too.
                    watch.cancel();

                    //Use finished progress, but add a few changes below.
                    showFinishedProgress();

                    //Change button text and allow user to go back now
                    dryerButton.setText("Start");
                    backButton.setEnabled(true);

                    //Cancels work
                    WorkManager.getInstance(getActivity()).cancelAllWorkByTag("Dryer");
                    WorkManager.getInstance(getActivity()).cancelAllWorkByTag("Notify");

                    Log.i("Dryer Fragment Confirm", "Timer and Workers Canceled");

                }

                Log.i("Dryer Fragment Confirm", "Confirm Finished");
            }
        });

        //Navigate User to Setting Fragment by clicking the Back Button
        binding.dryerBackButton.setOnClickListener(viewBack ->
        {
            Log.i("Dryer Fragment Back", "Back Clicked");

            //Specify the navigation action
            NavHostFragment.findNavController(DryerFragment.this)
                    .navigate(R.id.action_dryerFragment_to_HomeFragment);
        });



    //Will show the status when the background task is called
        mySavedWorkerInfo.observe(getViewLifecycleOwner(), listOfWorkInfos ->
    {

        Log.i("Dryer Fragment Info", "Dryer Info Started");

        //If work is null or empty then do nothing.
        if (listOfWorkInfos == null || listOfWorkInfos.isEmpty())
        {
            Log.i("Dryer Fragment Info", "No Worker Changes Found");
            return;
        }

        //The Tagged LiveData will get called here
        WorkInfo workInfo = listOfWorkInfos.get(0);

        //LiveData persists after first called. This keeps it from updating until getting called first.
        if (!firstCall)
        {
            Log.i("Dryer Fragment Info", "Worker Changes Found");

            //Check if background task is finished.
            boolean finished = workInfo.getState().isFinished();

            if (!finished)
            {
                Log.i("Dryer Fragment Info", "Worker in progress");

                //Change texts and buttons to show adjust is in progress
                showInProgress();

            }
            else
            {
                Log.i("Dryer Fragment Info", "Worker Stopped");

                //Pull number from clients output
                int errorNumber = workInfo.getOutputData().getInt("dryerOutput", 3);


                //Check if the client returned properly
                if(errorNumber == 0)
                {
                    Log.i("Dryer Fragment Info", "Worker Succeeded");
                    //Change text and buttons to show Adjust has finished.
                    showFinishedProgress();



                }
                else if (errorNumber == 1)
                {
                    Log.w("Dryer Fragment Info", "Worker Failed");
                    firstCall = true;
                    dryerStatusOff = true;
                    //Change texts and buttons to show adjust failed
                    showFailure();

                }
                else if (errorNumber == 2)
                {
                    Log.w("Dryer Fragment Info", "Worker Failed");
                    firstCall = true;
                    dryerStatusOff = true;
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

        Log.i("Dryer Frag Destroy", "Destroyed View");
        //Cancels work and Timer
        WorkManager.getInstance(getActivity()).cancelAllWorkByTag("Dryer");
        WorkManager.getInstance(getActivity()).cancelAllWorkByTag("Notify");
        watch.cancel();
        binding = null;
    }


    /**
     * Visually change the buttons, text, and anything else on the screen
     * to show that the dryer has stopped spinning.
     */
    public void showFinishedProgress()
    {
        Log.i("Dryer Fragment", "Change View to Success");

        //Text Changes
        dryerText.setText("Your Dryer Has Stopped!");

        //Dryer Button Changes
        dryerButton.setText("I got my Laundry");
        dryerButton.setBackgroundColor(getResources().getColor(R.color.green_grey));

        //Back Button Changes
        backButton.setBackgroundColor(getResources().getColor(R.color.green_grey));
    }

    /**
     * Visually change the buttons, text, and anything else on the screen
     * to show that the phone is waiting on word from the dryer reminder.
     */
    public void showInProgress()
    {
        Log.i("Dryer Fragment", "Change View to In Progress");

        //Text Changes
        dryerText.setText("Dryer is spinning");

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
        Log.i("Dryer Fragment", "Change View to Failure");

        //Text Changes
        dryerText.setText("Dryer Detection failed");

        //Dryer Button Changes
        dryerButton.setText("Start");
        dryerButton.setBackgroundColor(getResources().getColor(R.color.red_grey));

        //Back Button Changes
        backButton.setBackgroundColor(getResources().getColor(R.color.red_grey));
        backButton.setEnabled(true);
    }


}

