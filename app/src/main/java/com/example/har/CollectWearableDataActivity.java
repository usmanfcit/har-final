package com.example.har;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * CollectWearableDataActivity allows the user to choose between becoming a Bluetooth server
 * or a wearable device to collect sensory data.
 */
public class CollectWearableDataActivity extends AppCompatActivity {

    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Note: Otherwise, it is null.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_wearable_data);

        Button btnBecomeServer = findViewById(R.id.btnBecomeServer);
        Button btnBecomeWearable = findViewById(R.id.btnBecomeWearable);

        btnBecomeServer.setOnClickListener(new View.OnClickListener() {

            /**
             * Navigates to the BecomeBluetoothServerActivity.
             */
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(CollectWearableDataActivity.this, BecomeBluetoothServerActivity.class);
                startActivity(intent);
            }
        });

        btnBecomeWearable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /**
                 * Navigates to the WearableSensoryDataActivity.
                 */
                Intent intent = new Intent(CollectWearableDataActivity.this, WearableSensoryDataActivity.class);
                startActivity(intent);
            }
        });
    }
}
