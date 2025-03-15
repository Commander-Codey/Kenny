package com.example.notificationmanipulator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import static com.example.notificationmanipulator.NotificationInterceptorService.NOTIFICATION_BROADCAST;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private ArrayList<String> notificationList = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        loadStoredNotifications();

        // Register broadcast receiver to update UI when new notifications arrive
        registerReceiver(notificationReceiver, new IntentFilter(NOTIFICATION_BROADCAST), Context.RECEIVER_NOT_EXPORTED);


        // Button to open notification access settings
        Button btnEnable = findViewById(R.id.btnEnable);
        btnEnable.setOnClickListener(v -> {
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        });

        // Button to extract notification log
        Button btnExtract = findViewById(R.id.btnExtract);
        btnExtract.setOnClickListener(v -> exportNotificationLog());
    }

    private void loadStoredNotifications() {
        notificationList.clear();
        File file = new File(getExternalFilesDir(null), "notifications/notification_log.txt");
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file);
                 Scanner scanner = new Scanner(fis)) {
                while (scanner.hasNextLine()) {
                    notificationList.add(scanner.nextLine());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void exportNotificationLog() {
        File file = new File(getExternalFilesDir(null), "notifications/notification_log.txt");
        if (file.exists()) {
            Toast.makeText(this, "Log saved at: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "No log found!", Toast.LENGTH_SHORT).show();
        }
    }

    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadStoredNotifications(); // Refresh UI when new notification is stored
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(notificationReceiver);
    }
}