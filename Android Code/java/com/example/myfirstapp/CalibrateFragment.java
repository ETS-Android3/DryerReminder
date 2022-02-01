package com.example.myfirstapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myfirstapp.databinding.FragmentCalibrateBinding;
import com.example.myfirstapp.model.AxesModel;
import com.example.myfirstapp.model.ClientModel;
import com.example.myfirstapp.presenter.CalibrateContract;
import com.example.myfirstapp.presenter.CalibratePresenter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;


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
    boolean isBusy; //Will use this in the feature to keep the user form pressing buttons

    //Call the presenter
    private CalibrateContract.Presenter presenter;

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


        isBusy = false;


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

        //Instantiate the presenter
        presenter = new CalibratePresenter(this);

        Button calibrateButton = view.findViewById(R.id.calibrate_device_button);
        Button backButton = view.findViewById(R.id.calibrate_back_button);

        //Confirm the user wants to call the API to have the Pi Calibrate and send the data back to the phone.
        calibrateButton.setOnClickListener(new View.OnClickListener()
        {


            @Override
            public void onClick(View view)
            {

                //calibrateButton.setVisibility(View.INVISIBLE);
               // backButton.setEnabled(false);

                //Checks to see if device is busy. If not then set to true until method is finished.

                    //Attempt to connect to pi and save information to text file.
                    try
                    {

                    //If the version of Android is too old, then prevent the button from running.
                    int SDK_INT = android.os.Build.VERSION.SDK_INT;
                    if (SDK_INT > 8)
                    {
                        System.out.println("----------------------------Start");
                        //Call the presenter to open a connection the pi

                        //Run calibrate and save number. Anything other then 0 is an error
                        int errorNumber = presenter.doCalibrate();

                        //Change text base
                        if (errorNumber == 0)
                        {
                            showBasicText.setText("Calibration has finished");

                        }
                        else
                        {
                            showBasicText.setText("Calibration Failed. Make sure device is on.");
                        }

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
     * Take Calibrated range from the Raspberry Pi and save it to a text file to use it later.
     * Currently reads the same text file to show it has been created. That will be moved to Dryer Fragment later
     * @param savedRange Save range that was calibrated from pi
     *
     */
    @Override
    public void writeToFile(AxesModel savedRange)
    {
        System.out.println("Write to File-----------------------");
        //Try to write to a text file
        try
        {
            //Write saved range to config Calibrate text file
            String filename = "configCalibrate.txt";
            String fileContents = (savedRange.getAxisX() + "\n" + savedRange.getAxisY() + "\n" + savedRange.getAxisZ() + "\n");

            try (FileOutputStream fileOutput = getContext().openFileOutput(filename, Context.MODE_PRIVATE))
            {
                fileOutput.write(fileContents.getBytes(StandardCharsets.UTF_8));
            }

        }
        catch(IOException e)
        {
            System.out.println(e);
        }

        //Reading the file Move to Dryer Later
        //Converts text file numbers to axes model
        AxesModel newRange = new AxesModel();

        //Create the file input stream
        FileInputStream fileInput = null;

        //Try to find text file
        try
        {
            fileInput = getContext().openFileInput("configCalibrate.txt"); //Use context to read from text file
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

            //Convert the range to an axesModel
            newRange.setAxisX((Double.parseDouble(line)));
            line = reader.readLine();
            newRange.setAxisY((Double.parseDouble(line)));
            line = reader.readLine();
            newRange.setAxisZ((Double.parseDouble(line)));

            String contents = stringBuilder.toString();
            System.out.println("File Found:" + newRange.getAxisX() + " " + newRange.getAxisY() + " " + newRange.getAxisZ());
        }
        catch (IOException e)
        {
            System.out.println(e);
        }

    }


}