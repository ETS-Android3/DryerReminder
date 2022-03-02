package com.example.myfirstapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myfirstapp.databinding.FragmentNotifyBinding;
import com.example.myfirstapp.model.AxesModel;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Notify Fragment relates to how often the phone reminds the user their device
 * has detected no movement.
 *
 */
public class NotifyFragment extends Fragment
{

    private FragmentNotifyBinding binding;
    private Spinner spinner;


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
        binding = FragmentNotifyBinding.inflate(inflater, container, false);
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

        //Confirm the time the user wants to be reminded
        view.findViewById(R.id.notify_confirm_button).setOnClickListener(new View.OnClickListener()
         {
             @Override
             public void onClick(View view)
             {

                 writeToFile(spinner.getSelectedItem().toString());
             }
         });


        //Navigate User to Setting Fragment by clicking the Back Button
        binding.notifyBackButton.setOnClickListener(viewBack ->
        {
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
        binding = null;
    }

    /**
     * FIIIIIXXXXXXXXXXXXXXXXXXX
     * Take Calibrated range from the Raspberry Pi and save it to a text file to use it later.
     * Currently reads the same text file to show it has been created. That will be moved to Dryer Fragment later
     * @param notifyText Save range that was calibrated from pi
     *
     */
    public void writeToFile(String notifyText)
    {
        System.out.println("Write to File-----------------------");
        //Try to write to a text file
        try
        {
            //Write saved range to config Notify text file
            String filename = "configNotify.txt";
            String fileContents = notifyText;

            System.out.println(fileContents);
            try (FileOutputStream fileOutput = getActivity().openFileOutput(filename, Context.MODE_PRIVATE))
            {
                fileOutput.write(fileContents.getBytes(StandardCharsets.UTF_8));
            }

        }
        catch(IOException e)
        {
            System.out.println(e);
        }

        //Reading the file Move to Dryer Later------------------------------

        //Create the file input stream
        FileInputStream fileInput = null;

        //Try to find text file
        try
        {
            fileInput = getActivity().openFileInput("configNotify.txt"); //Use context to read from text file
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
            System.out.println(line);

            //Convert the range to an axesModel
            String textRead = line;


            String contents = stringBuilder.toString();
            System.out.println("File Found:" + textRead);
        }
        catch (IOException e)
        {
            System.out.println(e);

        }

    }


}

