package com.example.myfirstapp;

import android.app.Notification;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myfirstapp.databinding.FragmentHomeBinding;


import java.io.IOException;
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

        view.findViewById(R.id.settings_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                Log.i("Home Fragment Settings", "Settings Button Clicked");

                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_HomeFragment_to_settingsFragment);
            }
        });


        view.findViewById(R.id.dryer_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                Log.i("Home Fragment Dryer", "Dryer Button Clicked");

                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_HomeFragment_to_dryerFragment);
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

}