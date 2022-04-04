package com.example.myfirstapp;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.navigation.fragment.NavHostFragment;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.myfirstapp.background.CalibrateWorker;
import com.example.myfirstapp.databinding.FragmentCalibrateBinding;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;


/**
 * Calibrate Fragment that will allow the user to calibrate the Raspberry Pi's
 * accelerometer and save the data to the phone.
 *
 */
public class CalibrateFragment extends Fragment
{
    //Variables based on front-end items
    private FragmentCalibrateBinding binding;
    private TextView showBasicText;
    private TextView showBasicText2;
    private Button calibrateButton;
    private Button backButton;



    //Call Worker and Live Data with Worker Info
    private WorkManager myClientManager;
    private LiveData<List<WorkInfo>> mySavedWorkerInfo;

    private boolean firstCall; //Checks if the device has been called

    /**
     * Setup the Calibrate page with the layout, view, view group, etc.
     *
     * @param inflater converts the XML file into a view
     * @param container Creates the layout design for the view and View Group
     * @param savedInstanceState Holds the data for mapping values like strings
     * @return binding Binding class used with the Calibrate XML file
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        Log.i("Calibrate Fragment", "Created");

        //Set to true when user first comes to page
        firstCall = true;

        //Initialize WorkerManager and WorkerInfo with tag Calibrate
        myClientManager = WorkManager.getInstance(getActivity().getApplicationContext());
        mySavedWorkerInfo = myClientManager.getWorkInfosByTagLiveData("Calibrate");

        //Binding Fragment and front end items
        binding = FragmentCalibrateBinding.inflate(inflater, container, false);
        showBasicText = binding.getRoot().findViewById(R.id.calibrate_text_view1);
        showBasicText2 = binding.getRoot().findViewById(R.id.calibrate_text_view2);

        if(readFromFile() == 0)
        {
            showBasicText2.setVisibility(View.VISIBLE);
        }

        return binding.getRoot();

    }

    /**
     * Create all the actions for the Calibrate view.
     *
     * @param view Creates the Calibrate user interface
     * @param savedInstanceState Holds the data for mapping values like strings
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        //Initialize calibrate and back button
        calibrateButton = view.findViewById(R.id.calibrate_device_button);
        backButton = view.findViewById(R.id.calibrate_back_button);

        //Listens for a button click for the calibrate button
        calibrateButton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {
                Log.i("Calibrate Frag Confirm", "Confirm Clicked"); //Fragment shortened to fit in constraint

                //Set to false so device can show in progress to user
                firstCall = false;

                //Attempt to connect to pi through the Work Manager
                try
                {
                    //If the version of Android is too old, then prevent the button from running.
                    int SDK_INT = android.os.Build.VERSION.SDK_INT;
                    if (SDK_INT > 8)
                    {
                        Log.i("Calibrate Frag Confirm", "SDK is Above 8");


                        //Setup work request using the calibrate worker and tag it under calibrate.
                        OneTimeWorkRequest calibrateWorker = new OneTimeWorkRequest.Builder(CalibrateWorker.class)
                                .addTag("Calibrate")
                                .build();

                        //Start the calibrate background task
                        WorkContinuation start = myClientManager.beginUniqueWork("CalibrateDevice", ExistingWorkPolicy.KEEP, calibrateWorker);
                        start.enqueue();

                        Log.i("Calibrate Frag Confirm", "Calibrate Worker Built And Started");

                    }
                    else
                    {

                        //Create and show a toast that says the device's SDK is out of date
                        Log.i("Calibrate Frag Confirm", "SDK is 8 or Below");
                        Toast myToast = Toast.makeText(getActivity(), "Outdated Version. Cannot connect to device", Toast.LENGTH_LONG);
                        myToast.show();
                    }
                }
                catch (Exception e)
                {
                    //If exception is caught show failure to user
                    showFailure();
                    Log.w("Calibrate Frag Confirm", "Worker Not Started");
                    Log.e("Calibrate Frag Confirm", e.toString());
                }

                Log.i("Calibrate Frag Confirm", "Confirm Finished");

            }
        });

        //Navigate User to Settings Fragment by clicking the Back Button
        backButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View view)
            {
                Log.i("Calibrate Fragment Back", "Back Clicked");
                NavHostFragment.findNavController(CalibrateFragment.this).navigate(CalibrateFragmentDirections.actionCalibrateFragmentToSettingsFragment());

            }
        });


        //Listen to the LiveData to determine the status of the background task
        mySavedWorkerInfo.observe(getViewLifecycleOwner(), listOfWorkInfos ->
        {

            Log.i("Calibrate Fragment Info", "Calibrate Info Started");

            //If work is null or empty then do nothing.
            if (listOfWorkInfos == null || listOfWorkInfos.isEmpty())
            {
                Log.i("Calibrate Fragment Info", "No Worker Changes Found");
                return;
            }

            //The Tagged LiveData will get called here
            WorkInfo workInfo = listOfWorkInfos.get(0);

            //LiveData persists after it is first updated. This keeps it from updating until getting called first.
            if (!firstCall)
            {
                Log.i("Calibrate Fragment Info", "Worker Changes Found");

                //Check if background task is finished.
                boolean finished = workInfo.getState().isFinished();

                if (!finished)
                {
                    Log.i("Calibrate Fragment Info", "Worker in progress");

                    //Change texts and buttons to show calibrate is in progress
                    showInProgress();

                }
                else
                {
                    Log.i("Calibrate Fragment Info", "Worker Stopped");

                    //Pull number from clients output to check for errors
                    int errorNumber = workInfo.getOutputData().getInt("calibrateOutput", 3);


                    //Check if the client returned properly
                    if(errorNumber == 0)
                    {
                        Log.i("Calibrate Fragment Info", "Worker Succeeded");
                        //Change text and buttons to show calibrate has finished.
                        showFinishedProgress();

                    }
                    else if (errorNumber == 1)
                    {
                        Log.w("Calibrate Fragment Info", "Worker Failed");
                        firstCall = true;

                        //Change texts and buttons to show calibrate failed
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
        mySavedWorkerInfo.removeObservers(getViewLifecycleOwner());
        Log.i("Calibrate Frag Destroy", "Destroyed View");
        binding = null;

    }

    /**
     * Visually change the buttons, text, and anything else on the screen
     * to show that the phone is trying to calibrate the device.
     */
    public void showInProgress()
    {
        Log.i("Calibrate Fragment", "Change View to In Progress");

        //Text Changes
        showBasicText.setText("        Device is calibrating  \nDO NOT TOUCH THE DEVICE!");

        //Might make text color turn more grey and change failed to this red
        //Calibrate Button Changes
        calibrateButton.setText("Calibrating");
        calibrateButton.setBackgroundColor(getResources().getColor(R.color.yellow_grey));
        calibrateButton.setEnabled(false);

        //Back Button Changes
        backButton.setEnabled(false);
        backButton.setBackgroundColor(getResources().getColor(R.color.yellow_grey));
    }

    /**
     * Visually change the buttons, text, and anything else on the screen
     * to show that the phone has finished calibrating.
     */
    public void showFinishedProgress()
    {
        Log.i("Calibrate Fragment", "Change View to Success");

        //Text Changes
        showBasicText.setText("Calibration has finished");

        //Calibrate Button Changes
        calibrateButton.setText("Calibrate Again");
        calibrateButton.setBackgroundColor(getResources().getColor(R.color.green_grey));
        calibrateButton.setEnabled(true);

        //Back Button Changes
        backButton.setEnabled(true);
        backButton.setBackgroundColor(getResources().getColor(R.color.green_grey));
    }

    /**
     * Visually change the buttons, text, and anything else on the screen
     * to show that the phone is trying to calibrate the device.
     */
    public void showFailure()
    {
        Log.i("Calibrate Fragment", "Change View to Failure");

        //Text Changes
        showBasicText.setText("Calibration has failed. Make sure device is on.");

        //Calibrate Button Changes
        calibrateButton.setText("Calibrate");
        calibrateButton.setBackgroundColor(getResources().getColor(R.color.red_grey));
        calibrateButton.setEnabled(true);

        //Back Button Changes
        backButton.setBackgroundColor(getResources().getColor(R.color.red_grey));
        backButton.setEnabled(true);
    }

    /**
     * Find calibrated range from a text file and return a number based on the result.
     * Will save it to the savedRange variable.
     *
     */
    private int readFromFile()
    {
        Log.i("Home Fragment Read", "Method has started");


        //Create the file input stream
        FileInputStream fileInput = null;

        //Try to find text file
        try
        {
            Log.i("Home Fragment Read", "Attempt to find file");
            fileInput = getActivity().openFileInput("configCalibrate.txt"); //Use context to read from text file

            //Take the text file and try to convert it to an AxesModel
            InputStreamReader inputStreamReader = new InputStreamReader(fileInput, StandardCharsets.UTF_8);
            try (BufferedReader reader = new BufferedReader(inputStreamReader))
            {
                Log.i("Home Fragment Read", "Attempt to read from file");

                String line = reader.readLine();



                Log.i("Home Fragment Read", "Text on file found");

            }
            catch (IOException e)
            {
                Log.w("Home Fragment Read", "Reading file failed");
                Log.e("Home Fragment Read", e.toString());

                return 1;
            }

        }
        catch (FileNotFoundException e)
        {
            Log.w("Dryer Worker Read", "Finding file failed");
            Log.e("Dryer Worker Read", e.toString());
            return 1;
        }

        return 0;

    }


}