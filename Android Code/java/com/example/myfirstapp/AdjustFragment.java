package com.example.myfirstapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.myfirstapp.databinding.FragmentAdjustBinding;



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
    int adjustNumber;

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

        binding = FragmentAdjustBinding.inflate(inflater, container, false);
        adjustSeekBar = binding.getRoot().findViewById(R.id.seekBar);
        adjustText = binding.getRoot().findViewById(R.id.adjustNumber);
        adjustNumber = 0;
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

        //Confirm the Adjust number and call the API to save it to the Pi.
        binding.adjustConfirmButton.setOnClickListener(viewConfirm -> NavHostFragment.findNavController(AdjustFragment.this)
        );

        //Navigate User to Home Fragment by clicking the Back Button
        binding.adjustBackButton.setOnClickListener(viewBack -> {
            //Specify the navigation action
            NavHostFragment.findNavController(AdjustFragment.this)
                    .navigate(R.id.action_adjustFragment_to_settingsFragment);
        });

        //Listener for when seek bar moves
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

