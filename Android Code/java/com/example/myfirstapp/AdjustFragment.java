package com.example.myfirstapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.navigation.fragment.NavHostFragment;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myfirstapp.background.AdjustWorker;
import com.example.myfirstapp.databinding.FragmentAdjustBinding;

import java.util.List;


/**
 * Adjust Fragment that will allow the user to change the offset the Raspberry Pi's
 * uses to determine if the detection needs to be more or less sensitive.
 *
 */
public class AdjustFragment extends Fragment
{

    private FragmentAdjustBinding binding;
    SeekBar adjustSeekBar;
    TextView adjustText;
    Button adjustButton;
    Button backButton;
    int adjustNumber;

    //Call Worker
    private WorkManager myClientManager;
    private LiveData<List<WorkInfo>> mySavedWorkerInfo;

    private boolean firstCall; //Checks if the device has been called

    /**
     * Setup the Adjust page with the layout, view, view group, etc.
     *
     * @param inflater           converts the XML file into a view
     * @param container          Creates the layout design for the view and View Group
     * @param savedInstanceState Holds the data for mapping values like strings
     * @return binding Binding class used with the Adjust XML file
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {

        //Set to true when user first comes to page
        firstCall = true;

        //Initialize WorkerManager and WorkerInfo
        myClientManager = WorkManager.getInstance(getActivity().getApplicationContext());
        mySavedWorkerInfo = myClientManager.getWorkInfosByTagLiveData("Adjust");

        binding = FragmentAdjustBinding.inflate(inflater, container, false);
        adjustSeekBar = binding.getRoot().findViewById(R.id.seekBar);
        adjustText = binding.getRoot().findViewById(R.id.adjustNumber);
        adjustNumber = 1;
        backButton = binding.getRoot().findViewById(R.id.adjust_back_button);
        return binding.getRoot();

    }

    /**
     * Create all the actions for the view.
     *
     * @param view               Creates the Adjust user interface
     * @param savedInstanceState Holds the data for mapping values like strings
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        adjustButton = view.findViewById(R.id.adjust_confirm_button);

        //Confirm the Adjust number and call the API to save it to the Pi.
        adjustButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                firstCall = false;
                //Attempt to connect to Pi and save the adjust value
                try
                {

                    //If the version of Android is too old, then prevent the button from running.
                    int SDK_INT = android.os.Build.VERSION.SDK_INT;
                    if (SDK_INT > 8)
                    {
                        System.out.println("----------------------------Start");


                        OneTimeWorkRequest AdjustWorker = new OneTimeWorkRequest.Builder(AdjustWorker.class)
                                .addTag("Adjust")
                                .setInputData(adjustInput())
                                .build();

                        WorkContinuation start = myClientManager.beginUniqueWork("Adjusting", ExistingWorkPolicy.KEEP, AdjustWorker);

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
                    System.out.println(e);
                }
                finally //Make sure device is freed, even after an error
                {
                    System.out.println("Button finished");
                }
            }
        });

        //Navigate User to Home Fragment by clicking the Back Button
        binding.adjustBackButton.setOnClickListener(viewBack -> {
            //Specify the navigation action
            NavHostFragment.findNavController(AdjustFragment.this)
                    .navigate(R.id.action_adjustFragment_to_settingsFragment);
        });

        //Listener for when seek bar moves.
        adjustSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {


                //Add one to the progress since it starts at 0.
                adjustNumber = progress + 1;

                //Based on the bars position, tell the user how sensitive the device will be
                if(adjustNumber == 1)
                {
                    adjustText.setText("Not Sensitive");
                }
                else if(adjustNumber == 2)
                {
                    adjustText.setText("Below Average Sensitivity");
                }
                else if(adjustNumber == 3)
                {
                    adjustText.setText("Average Sensitivity");
                }
                else if(adjustNumber == 4)
                {
                    adjustText.setText("Above Average Sensitivity");
                }
                else if(adjustNumber == 5)
                {
                    adjustText.setText("Extremely Sensitive");
                }
                else
                {
                    adjustText.setText("How?");
                }
            }

            //Needed for listener, but not implemented.
            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            { }
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
                    //Change texts and buttons to show calibrate is in progress
                    showInProgress();

                }
                else
                {

                    //Pull number from clients output
                    int errorNumber = workInfo.getOutputData().getInt("adjustOutput", 3);
                    System.out.println(errorNumber);

                    if(errorNumber == 0)
                    {
                        //Change text and buttons to show calibrate has finished.
                        showFinishedProgress();
                    }
                    else if (errorNumber == 1)
                    {
                        firstCall = true;
                        //Change texts and buttons to show adjust failed
                        adjustText.setText("Adjust failed");
                        showFailure();

                    }
                    else if (errorNumber == 2)
                    {
                        firstCall = true;
                        //Change texts and buttons to show adjust failed
                        adjustText.setText("Adjust failed. Could not connect to device.");
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

    private Data adjustInput()
    {
        Data.Builder builder = new Data.Builder();
        builder.putInt("adjustSeekbar", adjustNumber);

        return builder.build();
    }

    /**
     * Visually change the buttons, text, and anything else on the screen
     * to show that the phone is trying to calibrate the device.
     */
    public void showFinishedProgress()
    {
        //Text Changes
        adjustText.setText("Sensitivity was saved");

        //Might make text color turn more grey and change failed to this red
        //Calibrate Button Changes
        adjustButton.setText("Confirmed");
        adjustButton.setBackgroundColor(getResources().getColor(R.color.green_grey));
        adjustButton.setEnabled(true);

        //Back Button Changes
        backButton.setBackgroundColor(getResources().getColor(R.color.green_grey));
        backButton.setEnabled(true);
    }

    /**
     * Visually change the buttons, text, and anything else on the screen
     * to show that the phone is trying to calibrate the device.
     */
    public void showInProgress()
    {
        //Text Changes
        adjustText.setText("Saving Sensitivity");

        //Might make text color turn more grey and change failed to this red
        //Calibrate Button Changes
        adjustButton.setText("Saving");
        adjustButton.setBackgroundColor(getResources().getColor(R.color.yellow_grey));
        adjustButton.setEnabled(false);

        //Back Button Changes
        backButton.setBackgroundColor(getResources().getColor(R.color.yellow_grey));
        backButton.setEnabled(false);
    }

    /**
     * Visually change the buttons, text, and anything else on the screen
     * to show that the phone is trying to calibrate the device.
     */
    public void showFailure()
    {

        //Might make text color turn more grey and change failed to this red
        //Calibrate Button Changes
        adjustButton.setText("Adjust");
        adjustButton.setBackgroundColor(getResources().getColor(R.color.red_grey));
        adjustButton.setEnabled(true);

        //Back Button Changes
        backButton.setBackgroundColor(getResources().getColor(R.color.red_grey));
        backButton.setEnabled(true);
    }

}

