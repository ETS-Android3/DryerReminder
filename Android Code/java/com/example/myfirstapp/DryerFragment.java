package com.example.myfirstapp;

import android.app.Notification;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myfirstapp.databinding.FragmentDryerBinding;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


import static com.example.myfirstapp.app.CHANNEL_1_ID;


/**
 * Dryer Fragment allows user to call the Pi to get a notification when the Pi
 * detects no motion.
 *
 */
public class DryerFragment extends Fragment {

    private FragmentDryerBinding binding;

    //For Notifications
    private NotificationManagerCompat notificationManager;

    /**
     * Setup the Dryer page with the layout, view, view group, etc.
     *
     * @param inflater converts the XML file into a view
     * @param container Creates the layout design for the view and View Group
     * @param savedInstanceState Holds the data for mapping values like strings
     * @return binding Binding class used with the Dryer XML file
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {

        binding = FragmentDryerBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    /**
     * Create all the actions for the view.
     *
     * @param view Creates the Dryer user interface
     * @param savedInstanceState Holds the data for mapping values like strings
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        //Click Listener for Count
        //FOR USE FOR THE GET WITH A STRING!!!!!!!!!!!!!!!!!!!!!
        view.findViewById(R.id.dryer_confirm_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //countMe(view);
                int SDK_INT = android.os.Build.VERSION.SDK_INT;
                if (SDK_INT > 8) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();
                    StrictMode.setThreadPolicy(policy);

                    //Above needed for get to work
                    System.out.println("START OF THE CLICK--------------------------------------");
                    //Setup OkHttpClient
                    OkHttpClient client = new OkHttpClient();

                    //Setup the builder so that it does not time out after 10 seconds (Needed for dryer
                    //I believe write is the part that needs to wait awhile.
                    OkHttpClient.Builder builder = new OkHttpClient.Builder();
                    builder.connectTimeout(2, TimeUnit.HOURS) // connect timeout
                            .writeTimeout(2, TimeUnit.HOURS) // write timeout
                            .readTimeout(2, TimeUnit.HOURS); // read timeout

                    client = builder.build();

                    //Setup the request and pick the URL we are calling
                    Request getRequest = new Request.Builder()
                            //        .url("http://192.168.0.23:8090/DryPi/dryStop")  //Home
                            .url("http://172.24.184.117:8090/DryPi/dryStop")  //School
                            .build();
                    try {
                        System.out.println("START OF END--------------------------------------");
                        Response response = client.newCall(getRequest).execute();
                        System.out.println(response.body().string());
                        sendOnChannel1(view);

                        System.out.println("END OF THE CLICK--------------------------------------");
                    } catch (IOException e) {
                        System.out.println("Start OF THE CLICK BAD---------------------------------");
                        e.printStackTrace();

                    }
                }
            }
        });

        //Navigate User to Setting Fragment by clicking the Back Button
        binding.dryerBackButton.setOnClickListener(viewBack ->
        {
            //Specify the navigation action
            NavHostFragment.findNavController(DryerFragment.this)
                    .navigate(R.id.action_dryerFragment_to_HomeFragment);
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
     * Will be used to send notification when fully implemented Currently left behidn from prototype.
     * @param v
     */
    public void sendOnChannel1(View v)
    {
        String title = "Dryer Reminder";
        String message = "Your Dryer has finished!";
        Notification notification = new NotificationCompat.Builder(getActivity(), CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_one)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        notificationManager.notify(1, notification);
    }

}

