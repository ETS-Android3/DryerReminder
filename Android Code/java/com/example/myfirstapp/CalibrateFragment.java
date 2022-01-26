package com.example.myfirstapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myfirstapp.databinding.FragmentCalibrateBinding;
import com.example.myfirstapp.model.ClientModel;
import com.example.myfirstapp.presenter.CalibrateContract;
import com.example.myfirstapp.presenter.CalibratePresenter;


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
     *Create all the actions for the view.
     *
     * @param view Creates the Calibrate user interface
     * @param savedInstanceState Holds the data for mapping values like strings
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter = new CalibratePresenter(this);

        //Confirm the user wants to call the API to have the Pi Calibrate and send the data back to the phone.
        view.findViewById(R.id.calibrate_device_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                presenter.doCalibrate();

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
}