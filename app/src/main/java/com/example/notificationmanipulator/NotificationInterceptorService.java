package com.example.notificationmanipulator;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class NotificationInterceptorService extends NotificationListenerService {

    private static final String TAG = "NotificationInterceptor";
    public static final String NOTIFICATION_BROADCAST = "com.example.notificationmanipulator.NEW_NOTIFICATION";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();

        // Focus only on Gmail notifications
        if (!packageName.equals("com.google.android.gm")) {
            Notification notification = sbn.getNotification();
            CharSequence text = notification.extras.getCharSequence(Notification.EXTRA_TEXT);

            if (text != null) {
                String notificationText = text.toString();
                Log.d(TAG, "Notification from: " + packageName + " - " + notificationText);

                if (shouldHideNotification(packageName, notificationText)) {
                    cancelNotification(sbn.getKey()); // Hide notification
                    storeNotificationData(packageName, notificationText); // Store it

                    // Notify UI to refresh
                    Intent intent = new Intent(NOTIFICATION_BROADCAST);
                    sendBroadcast(intent);
                }
            }
        }

        else {
            Notification notification = sbn.getNotification();
            Bundle extras = notification.extras;

            // Extract Subject, Sender, and Email Preview (if available)
            String sender = extras.getString(Notification.EXTRA_TITLE);  // Sender's name
            String subject = extras.getCharSequence(Notification.EXTRA_TEXT).toString();  // Subject line
            String emailPreview = extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString();  // Partial email body

            Log.d(TAG, "Gmail Notification - Sender: " + sender + ", Subject: " + subject + ", Preview: " + emailPreview);

            // Store the extracted email data
                if (shouldHideNotification(packageName, subject)) {
                    cancelNotification(sbn.getKey()); // Hide notification
                    storeEmailData(sender, subject, emailPreview); // Store it

                    // Notify UI to refresh
                    Intent intent = new Intent(NOTIFICATION_BROADCAST);
                    sendBroadcast(intent);
            }
            // Hide the notification (Optional)
            cancelNotification(sbn.getKey());
        }
    }


    private boolean shouldHideNotification(String packageName, String text) {
        Log.d(TAG, "Hiding notification from: " + packageName); // Log which app’s notifications are hidden
        return true; // Hide all notifications
    }


    private void storeEmailData(String sender, String subject, String emailPreview) {
        File dir = new File(getExternalFilesDir(null), "emails");
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, "notification_log.txt");

        try (FileWriter writer = new FileWriter(file, true)) {
            writer.append("Sender: ").append(sender != null ? sender : "Unknown").append("\n");
            writer.append("Subject: ").append(subject).append("\n");
            writer.append("Preview: ").append(emailPreview != null ? emailPreview : "No preview available").append("\n\n");
            Log.d(TAG, "Email notification saved successfully!");
        } catch (IOException e) {
            Log.e(TAG, "Error saving email notification: " + e.getMessage());
        }
    }

    private void storeNotificationData(String packageName, String text) {
        File dir = new File(getExternalFilesDir(null), "notifications");
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, "notification_log.txt");
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.append("App: ").append(packageName).append("\n");
            writer.append("Text: ").append(text).append("\n\n");
        } catch (IOException e) {
            Log.e(TAG, "Error saving notification data", e);
        }
    }
}
