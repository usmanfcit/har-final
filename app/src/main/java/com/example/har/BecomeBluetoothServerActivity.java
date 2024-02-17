package com.example.har;

// Import statements for accessing Android framework classes
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;

// Import statements for handling background tasks and messaging
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

// Import statements for accessing Android UI components
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

// Import statements for accessing AndroidX components
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.net.URL;
import java.util.Set;


/**
 * Activity to become a Bluetooth server and handle connections with remote devices.
 */

public class BecomeBluetoothServerActivity extends AppCompatActivity {

    private EditText editTextServerUrl;
    private ListView listConnectedDevices;
    private Button btnConnect;
    private Button btnDisconnect;

   private BluetoothHandler bluetoothHandler;
   private SocketIO socketHandler;
   private ConnectionHandler connectionHandler;

    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down then this Bundle contains the data it most recently supplied
     *                           in onSaveInstanceState(Bundle).
     */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_become_bluetooth_server);

        editTextServerUrl = findViewById(R.id.editTextServerUrl);
        listConnectedDevices = findViewById(R.id.listConnectedDevices);
        btnConnect = findViewById(R.id.btnConnect);
        btnDisconnect = findViewById(R.id.btnDisconnect);

        initializeBluetooth();
        initSocketHandler();
        initConnectionHandler();

        btnConnect.setOnClickListener(view -> {
            String serverUrl = editTextServerUrl.getText().toString();
            if (!serverUrl.isEmpty() && Utility.isValidUrl(serverUrl)) {
                try {
                    URL url = new URL(serverUrl);
                    String host = url.getHost();
                    int port = url.getPort();
                    System.out.println("URL \n" + host + " " + port);
                    connectionHandler.connect(host, port);
                    //socketHandler.connect(serverUrl);
                } catch (Exception e) {
                    showToast("Error occurred: " + e.getMessage());
                }

                if(connectionHandler != null){
                    System.out.println("Not Null");
                    bluetoothHandler.startAcceptingConnection();
                }
            }
            else {
                showToast("Please enter a valid URL.");
                return;
            }
            showToast("Connection started.");
        });
        btnDisconnect.setOnClickListener(view -> {
            connectionHandler.disconnect();
            //socketHandler.stop();
        });
    }

    /**
     * Show a toast message.
     *
     * @param message The message to display.
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Initialize Bluetooth handling.
     */
    private void initializeBluetooth() {
        bluetoothHandler = new BluetoothHandler(this, new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg_type) {
                switch (msg_type.what) {
                    case Constants.MESSAGE_READ:
                        Log.d("Ac", "message received");
                        String receivedMessage = (String) msg_type.obj;
                        connectionHandler.sendMessage(receivedMessage);
                        //socketHandler.sendMessage(receivedMessage);
                        //updateReceivedMessage(receivedMessage);
                        break;
                    case Constants.CONNECTED:
                        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                        break;
                    case Constants.CONNECTING:
                        Toast.makeText(getApplicationContext(), "Connecting...", Toast.LENGTH_SHORT).show();
                        break;
                    case Constants.NO_SOCKET_FOUND:
                        Toast.makeText(getApplicationContext(), "No socket found", Toast.LENGTH_SHORT).show();
                        break;
                    case Constants.PAIRED_DEVICES:
                        updatePairedDevices();
                        break;
                    case Constants.SHOW_LOG:
                        Toast.makeText(getApplicationContext(), msg_type.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        // Check and request Bluetooth permissions
        if (!bluetoothHandler.checkBluetoothPermissions()) {
            bluetoothHandler.requestBluetoothPermissions();
        } else {
            // Permissions already granted, proceed with your initialization
            bluetoothHandler.startAcceptingConnection();
        }
    }

    /**
     * Initialize the socket handler.
     */
    private void initSocketHandler(){
        socketHandler = new SocketIO(this, new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg_type) {
                switch (msg_type.what) {
                    case Constants.SOCKET_CONNECTED:
                        showToast("Socket Connected Successfully");
                        btnConnect.setVisibility(View.GONE);
                        btnDisconnect.setVisibility(View.VISIBLE);
                        break;
                    case Constants.SOCKET_ERROR:
                        showToast("Error While Connecting To Server");;
                        break;
                }
            }
        });
    }

    /**
     * Initialize the connection handler.
     */
    private void initConnectionHandler(){
        connectionHandler = new ConnectionHandler(this, new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg_type) {
                switch (msg_type.what) {
                    case Constants.SOCKET_CONNECTED:
                        showToast("Socket Connected Successfully");
                        btnConnect.setVisibility(View.GONE);
                        btnDisconnect.setVisibility(View.VISIBLE);
                        break;
                    case Constants.SOCKET_DISCONNECTED:
                        showToast("Socket Disconnected Successfully");
                        btnConnect.setVisibility(View.VISIBLE);
                        btnDisconnect.setVisibility(View.GONE);
                    case Constants.SOCKET_ERROR:
                        showToast("Error While Connecting To Server");;
                        break;
                }
            }
        });
    }

    /**
     * Handle activity result.
     *
     * @param requestCode The request code.
     * @param resultCode  The result code.
     * @param data        The data returned.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth has been successfully enabled
                bluetoothHandler.checkAndEnableBluetooth();
            } else {
                Toast.makeText(this, "Bluetooth is required for this application. Exiting.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    /**
     * Update the list of paired Bluetooth devices.
     */
    private void updatePairedDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothHandler.getPairedDevices();
        System.out.println(pairedDevices);
        ArrayAdapter<String> adapter_paired_devices = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        for (BluetoothDevice device : pairedDevices) {
            adapter_paired_devices.add(device.getName() + "\n" + device.getAddress());
        }

        listConnectedDevices.setAdapter(adapter_paired_devices);
    }

    /**
     * Perform cleanup when activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
