package com.example.myfirstapp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class CalibrateFragment extends Fragment implements CalibrateContract.View {

    private FragmentCalibrateBinding binding;

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
            Bundle savedInstanceState
    ) {

        binding = FragmentCalibrateBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    /**
     *Create all the actions for the Calibrate view.
     *
     * @param view Creates the Calibrate user interface
     * @param savedInstanceState Holds the data for mapping values like strings
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Instantiate the presenter
        presenter = new CalibratePresenter(this);

        //Confirm the user wants to call the API to have the Pi Calibrate and send the data back to the phone.
        view.findViewById(R.id.calibrate_device_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //If the version of Android is too old, then prevent the button from running.
                int SDK_INT = android.os.Build.VERSION.SDK_INT;
                if (SDK_INT > 8)
                {

                    AxesModel test = new AxesModel(0.0064, 0.0420, 0.0169);
                    writeToFile(test);

                    //Try to connect to the device
                    //presenter.doCalibrate();



                }
                else
                {
                    //Create and show a toast that says the device's SDK is out of date
                    Toast myToast = Toast.makeText(getActivity(), "Outdated Version. Cannot connect to device", Toast.LENGTH_LONG);
                    myToast.show();
                }
            }
        });

        //Navigate User to Settings Fragment by clicking the Back Button
        binding.calibrateBackButton.setOnClickListener(viewBack -> {
            //Specify the navigation action
            NavHostFragment.findNavController(CalibrateFragment.this)
                    .navigate(R.id.action_calibrateFragment_to_settingsFragment);
        });

    }

    /**
     * Destroys the view by setting the binding to null.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    //Honestly, I have no fucking clue
    @Override
    public void showInUseError() {

    }

    public void writeToFile(AxesModel savedRange)
    {
        try
        {
            System.out.println("Work--------------------------------------Bitch");
            String filename = "myfile.txt";
            String fileContents = (savedRange.getAxisX() + "\n" + savedRange.getAxisY() + "\n" + savedRange.getAxisZ() + "\n");
            try (FileOutputStream fos = getContext().openFileOutput(filename, Context.MODE_PRIVATE)) {
                fos.write(fileContents.getBytes(StandardCharsets.UTF_8));
                fos.close();
            }

        }
        catch(IOException e)
        {
            System.out.println("Oppsie Whoppsie");
        }

        //Reading the file Move to Dryer Later
        //Converts text file numbers to axes model
        AxesModel newRange = new AxesModel();

        FileInputStream fis = null;
        try {
            fis = getContext().openFileInput("myfile.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader inputStreamReader =
                new InputStreamReader(fis, StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();

            newRange.setAxisX((Double.parseDouble(line)));
            line = reader.readLine();
            newRange.setAxisY((Double.parseDouble(line)));
            line = reader.readLine();
            newRange.setAxisZ((Double.parseDouble(line)));

        } catch (IOException e) {
            // Error occurred when opening raw file for reading.
        } finally {
            String contents = stringBuilder.toString();
            System.out.println(newRange.getAxisX() + " " + newRange.getAxisY() + " " + newRange.getAxisZ());
        }
    }
}