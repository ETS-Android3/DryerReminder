package com.example.myfirstapp;


import android.os.Bundle;
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
import com.example.myfirstapp.presenter.CalibrateContract;

import java.util.List;


/**
 * Calibrate Fragment that will allow the user to calibrate the Raspberry Pi's
 * accelerometer and save the data to the phone.
 *
 */
public class CalibrateFragment extends Fragment implements CalibrateContract.View
{
    //Variables
    private FragmentCalibrateBinding binding;
    TextView showBasicText;
    Button calibrateButton;
    Button backButton;

    //Call the presenter
    private CalibrateContract.Presenter presenter;

    //Call Worker
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

        //Set to true when user first comes to page
        firstCall = true;

        //Initialize WorkerManager and WorkerInfo
        myClientManager = WorkManager.getInstance(getActivity().getApplicationContext());
        mySavedWorkerInfo = myClientManager.getWorkInfosByTagLiveData("Calibrate");




        //Binding Fragment
        binding = FragmentCalibrateBinding.inflate(inflater, container, false);
        showBasicText = binding.getRoot().findViewById(R.id.calibrate_text_view1);
        return binding.getRoot();


    }

    /**
     *Create all the actions for the Calibrate view.
     *
     * @param view Creates the Calibrate user interface
     * @param savedInstanceState Holds the data for mapping values like strings
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        calibrateButton = view.findViewById(R.id.calibrate_device_button);
        backButton = view.findViewById(R.id.calibrate_back_button);

        //Confirm the user wants to call the API to have the Pi Calibrate and send the data back to the phone.
        calibrateButton.setOnClickListener(new View.OnClickListener()
        {


            @Override
            public void onClick(View view)
            {
                //Set to false so device can show in progress to user
                firstCall = false;

                //Attempt to connect to pi and save information to text file.
                try
                {

                    //If the version of Android is too old, then prevent the button from running.
                    int SDK_INT = android.os.Build.VERSION.SDK_INT;
                    if (SDK_INT > 8)
                    {
                        System.out.println("----------------------------Start");


                        OneTimeWorkRequest calibrateWorker = new OneTimeWorkRequest.Builder(CalibrateWorker.class)
                                .addTag("Calibrate")
                                .build();

                        WorkContinuation start = myClientManager.beginUniqueWork("stuff", ExistingWorkPolicy.KEEP, calibrateWorker);

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
                    showBasicText.setText("Calibration Failed. Make sure device is on in.");
                }
                finally //Make sure device is freed, even after an error
                {
                    System.out.println("Button finished");
                }


            }
        });

        //Navigate User to Settings Fragment by clicking the Back Button
        backButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View view)
            {
                NavHostFragment.findNavController(CalibrateFragment.this).navigate(CalibrateFragmentDirections.actionCalibrateFragmentToSettingsFragment());

            }
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
                if (!finished)
                {
                    //Change texts and buttons to show calibrate is in progress
                    showInProgress();
                }
                else
                {
                    //Change text and buttons to show calibrate has finished.
                    showFinishedProgress();
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
        binding = null;

    }


    /**
     * Show error that device is already in use.
     */
    @Override
    public void showInUseError()
    {
        //Make toast to tell user device is in use.
        Toast myToast = Toast.makeText(getActivity(), "Device already in use.", Toast.LENGTH_LONG);
        myToast.show();
    }

    /**
     * Visually change the buttons, text, and anything else on the screen
     * to show that the phone is trying to calibrate the device.
     */
    public void showInProgress()
    {
        //Text Changes
        showBasicText.setText("        Device is calibrating  \nDO NOT TOUCH THE DEVICE!");

        //Might make text color turn more grey and change failed to this red
        //Calibrate Button Changes
        calibrateButton.setText("Calibrating");
        calibrateButton.setBackgroundColor(getResources().getColor(R.color.red_grey));
        calibrateButton.setEnabled(false);

        //Back Button Changes
        backButton.setEnabled(false);
        backButton.setBackgroundColor(getResources().getColor(R.color.red_grey));
    }

    /**
     * Visually change the buttons, text, and anything else on the screen
     * to show that the phone has finished calibrating.
     */
    public void showFinishedProgress()
    {
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




}