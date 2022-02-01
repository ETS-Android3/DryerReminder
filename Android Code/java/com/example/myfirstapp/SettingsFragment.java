package com.example.myfirstapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myfirstapp.databinding.FragmentSettingsBinding;

/**
 * Settings Fragment dedicated to navigating the user to parts of the app that
 * handle changing settings on the Raspberry Pi or Phone app.
 *
 */
public class SettingsFragment extends Fragment
{

    private FragmentSettingsBinding binding;

    /**
     * Setup the Settings page with the layout, view, view group, etc.
     *
     * @param inflater converts the XML file into a view
     * @param container Creates the layout design for the view and View Group
     * @param savedInstanceState Holds the data for mapping values like strings
     * @return binding Binding class used with the Settings XML file
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    /**
     *Create all the actions for the view.
     *
     * @param view Creates the Settings user interface
     * @param savedInstanceState Holds the data for mapping values like strings
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        //Navigate User to Adjust Fragment by clicking the Adjust Button
        binding.adjustButton.setOnClickListener(viewAdjust -> NavHostFragment.findNavController(SettingsFragment.this)
                .navigate(R.id.action_settingsFragment_to_adjustFragment));

        //Navigate User to Calibrate Fragment by clicking the Calibrate Button
        binding.calibrateButton.setOnClickListener(viewCalibrate ->
        {
            //Specify the navigation action
            NavHostFragment.findNavController(SettingsFragment.this)
                    .navigate(R.id.action_settingsFragment_to_calibrateFragment);
        });

        //Navigate User to Notify Fragment by clicking the calibrate Button
        binding.notificationButton.setOnClickListener(viewNotify ->
        {

            //Specify the navigation action
            NavHostFragment.findNavController(SettingsFragment.this)
                    .navigate(R.id.action_settingsFragment_to_notifyFragment);
        });

        //Navigate User to Home Fragment by clicking the Back Button
        binding.settingsBackButton.setOnClickListener(viewBack ->
        {
            //Specify the navigation action
            NavHostFragment.findNavController(SettingsFragment.this)
                    .navigate(R.id.action_settingsFragment_to_HomeFragment);
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

}