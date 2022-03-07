package com.example.myfirstapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myfirstapp.databinding.FragmentNotifyBinding;

import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.charset.StandardCharsets;

/**
 * Notify Fragment relates to how often the phone reminds the user their device
 * has detected no movement.
 *
 */
public class NotifyFragment extends Fragment
{
    //Variables based on front-end items
    private FragmentNotifyBinding binding;
    private Spinner spinner;
    private TextView notifyText;
    private Button notifyButton;
    private Button backButton;


    /**
     * Setup the Notify page with the layout, view, view group, etc.
     *
     * @param inflater converts the XML file into a view
     * @param container Creates the layout design for the view and View Group
     * @param savedInstanceState Holds the data for mapping values like strings
     * @return binding Binding class used with the Notify XML file
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    )
    {
        Log.i("Notify Fragment", "Created");

        //Binding Fragment and front end items
        binding = FragmentNotifyBinding.inflate(inflater, container, false);
        notifyText = binding.getRoot().findViewById(R.id.notifyView);
        backButton = binding.getRoot().findViewById(R.id.notify_back_button);

        //Initialize, Define, and Populate the spinner
        spinner = binding.getRoot().findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.minutes_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        return binding.getRoot();

    }

    /**
     * Create all the actions for the view.
     *
     * @param view Creates the Notify user interface
     * @param savedInstanceState Holds the data for mapping values like strings
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Initialize Notify Button
        notifyButton = view.findViewById(R.id.notify_confirm_button);

        //Confirm the time the user wants to be reminded
        view.findViewById(R.id.notify_confirm_button).setOnClickListener(new View.OnClickListener()
         {
             @Override
             public void onClick(View view)
             {
                 Log.i("Notify Fragment Confirm", "Confirm Clicked");
                 writeToFile(spinner.getSelectedItem().toString());
             }
         });


        //Navigate User to Setting Fragment by clicking the Back Button
        binding.notifyBackButton.setOnClickListener(viewBack ->
        {
            Log.i("Notify Fragment Back", "Back Clicked");

            //Specify the navigation action
            NavHostFragment.findNavController(NotifyFragment.this)
                    .navigate(R.id.action_notifyFragment_to_settingsFragment);
        });

    }

    /**
     * Destroys the view by setting the binding to null.
     */
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        Log.i("Notify Fragment Destroy", "Destroyed View");
        binding = null;
    }

    /**
     * Take Text from the spinner save it to a text file to use it later.
     * @param notifyText String that will be saved to file
     *
     */
    public void writeToFile(String notifyText)
    {
        Log.i("Notify Fragment Write", "Method Started");
        Log.i("Notify Fragment Write", "Spinner Text - " + notifyText);

        //Try to write to a text file
        try
        {
            Log.i("Notify Fragment Write", "Try to Write to File");

            //Write spinner text to Config Notify text file
            String filename = "configNotify.txt";
            String fileContents = notifyText;

            try (FileOutputStream fileOutput = getActivity().openFileOutput(filename, Context.MODE_PRIVATE))
            {
                fileOutput.write(fileContents.getBytes(StandardCharsets.UTF_8));
            }
            showFinishedProgress();
            Log.i("Notify Fragment Write", "Write Successful");

        }
        catch(IOException e)
        {
            showFailure();
            Log.w("Notify Fragment Write", "Write Failed");
            Log.e("Notify Fragment Write", e.toString());
        }

    }

    /**
     * Visually change the buttons, text, and anything else on the screen
     * to show that the phone saved their settings
     */
    public void showFinishedProgress()
    {
        Log.i("Notify Fragment", "Change View to Success");

        //Text Changes
        notifyText.setText("Settings Saved");

        //Might make text color turn more grey and change failed to this red
        //Notify Button Changes
        notifyButton.setText("Saved");
        notifyButton.setBackgroundColor(getResources().getColor(R.color.green_grey));

        //Back Button Changes
        backButton.setBackgroundColor(getResources().getColor(R.color.green_grey));

    }

    /**
     * Visually change the buttons, text, and anything else on the screen
     * to show that the phone failed to save the file.
     */
    public void showFailure()
    {
        Log.i("Notify Fragment", "Change View to Failed");

        //Text Changes
        notifyText.setText("Failed to Save");

        //Notify Button Changes
        notifyButton.setText("Error");
        notifyButton.setBackgroundColor(getResources().getColor(R.color.red_grey));

        //Back Button Changes
        backButton.setBackgroundColor(getResources().getColor(R.color.red_grey));

    }

}

