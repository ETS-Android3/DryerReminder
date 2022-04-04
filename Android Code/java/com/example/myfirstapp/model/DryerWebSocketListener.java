package com.example.myfirstapp.model;

import android.util.Log;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 *A WebSocket Listener that connects to the Raspberry Pi and tell it to start checking if the dryer has stopped.
 * Updates a number and with a getter other classes can see if a message has been received yet.
 *
 * Data: 04/01/22
 */
public class DryerWebSocketListener extends WebSocketListener
{
    private static final int NORMAL_CLOSURE_STATUS = 1000;

    //The Status of the dryer and if the websockets has received a message
    int statusNumber = -1;

    /**
     * This is called when a connection to the websockets has been made.
     * @param webSocket The websockets that connects to the server
     * @param response Response from client
     */
    @Override
    public void onOpen(WebSocket webSocket, Response response)
    {
        Log.i("Dryer Socket Listener", "Connection with Socket Made");

    }

    /**
     * Called when a message is received from the server. If it is the right text that means
     * the dryer has stopped. If not then assume an error occurred on the Pi.
     *
     * @param webSocket The Websockets that connects to the server
     * @param text The Text that is send
     */
    @Override
    public void onMessage(WebSocket webSocket, String text)
    {
        Log.i("Dryer Socket Listener", "Message Received " + text);

        //If the pi returns the right message then changes status to 0
        if (text.equals("Dryer Finished"))
        {
            Log.i("Dryer Socket Listener", "Dryer has finished");
            statusNumber =  0;
        }
        else //if the wrong message is sent then return 1, meaning it failed
        {
            Log.w("Dryer Socket Listener", "An Error Occurred with the Pi");
            statusNumber = 1;
        }
        webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye!");

    }


    /**
     * Called when the connection to the server is closing.
     *
     * @param webSocket Websocket that is connected to the server
     * @param code Code of the closing status
     * @param reason Why the socket is closing
     */
    @Override
    public void onClosing(WebSocket webSocket, int code, String reason)
    {
        webSocket.close(NORMAL_CLOSURE_STATUS, null);
        Log.i("Dryer Socket Listener", "Closing: " + code + " " + reason);
    }

    /**
     *
     * @param webSocket Socket that was connected to the server
     * @param thrown Reason the failure happened
     * @param response Response from client
     */
    @Override
    public void onFailure(WebSocket webSocket, Throwable thrown, Response response)
    {
        Log.e("Dryer Socket Listener", "Error Occurred From: " + thrown);
    }

    /**
     * A Getter that can be used to inform other classes if the websockets has
     * received a message from the Pi.
     *
     * @return An int which is the status of the dryer.
     */
    public int getStatusNumber()
    {
        return statusNumber;
    }

}

