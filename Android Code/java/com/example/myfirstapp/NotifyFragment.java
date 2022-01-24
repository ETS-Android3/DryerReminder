package com.example.myfirstapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myfirstapp.databinding.FragmentNotifyBinding;

/**
 * Notify Fragment relates to how often the phone reminds the user their device
 * has detected no movement.
 *
 */
public class NotifyFragment extends Fragment {

    private FragmentNotifyBinding binding;

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
    ) {

        binding = FragmentNotifyBinding.inflate(inflater, container, false);
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
        binding.notifyConfirmButton.setOnClickListener(viewConfirm -> NavHostFragment.findNavController(NotifyFragment.this)
        );

        //Navigate User to Setting Fragment by clicking the Back Button
        binding.notifyBackButton.setOnClickListener(viewBack -> {
            //Specify the navigation action
            NavHostFragment.findNavController(NotifyFragment.this)
                    .navigate(R.id.action_notifyFragment_to_settingsFragment);
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

}

