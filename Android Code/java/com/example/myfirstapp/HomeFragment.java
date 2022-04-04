package com.example.myfirstapp;

import android.app.Notification;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myfirstapp.databinding.FragmentHomeBinding;
import com.example.myfirstapp.model.AxesModel;
import com.example.myfirstapp.model.WebTestCalled;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static com.example.myfirstapp.app.CHANNEL_1_ID;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class HomeFragment extends Fragment
{

    //Variables
    private FragmentHomeBinding binding;
    private NotificationManagerCompat notificationManager;

    TextView showCountTextView;


    /**
     * Setup the Home page with the layout, view, view group, etc.
     *
     * @param inflater converts the XML file into a view
     * @param container Creates the layout design for the view and View Group
     * @param savedInstanceState Holds the data for mapping values like strings
     * @return binding Binding class used with the Home XML file
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        Log.i("Home Fragment", "Created");

        // Inflate the layout for this fragment
        View fragmentFirstLayout = inflater.inflate(R.layout.fragment_home, container, false);


        return fragmentFirstLayout;
    }


    /**
     * Create all the actions for the view.
     *
     * @param view Creates the Notify user interface
     * @param savedInstanceState Holds the data for mapping values like strings
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        //Go to settings page
        view.findViewById(R.id.settings_button).setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view) {
                Log.i("Home Fragment Settings", "Settings Button Clicked");

                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_HomeFragment_to_settingsFragment);
            }
        });

        //Go to Dryer or Calibrate page
        view.findViewById(R.id.dryer_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                Log.i("Home Fragment Dryer", "Dryer Button Clicked");

                //Check that file is found before allow user to dryer page
                if(readFromFile() == 0)
                {
                    NavHostFragment.findNavController(HomeFragment.this)
                            .navigate(R.id.action_HomeFragment_to_dryerFragment);
                }
                else
                {
                    //Create and show a toast that says the device's SDK is out of date
                    Log.i("Home Fragment Dryer", "Calibrate File not found");
                    Toast myToast = Toast.makeText(getActivity(), "Calibration not done, please calibrate device", Toast.LENGTH_LONG);
                    myToast.show();

                    NavHostFragment.findNavController(HomeFragment.this)
                            .navigate(R.id.action_HomeFragment_to_calibrateFragment);

                }

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
        Log.i("Home Fragment Destroy", "Destroyed View");
        binding = null;
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