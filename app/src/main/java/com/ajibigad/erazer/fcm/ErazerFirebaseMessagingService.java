package com.ajibigad.erazer.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ajibigad.erazer.R;
import com.ajibigad.erazer.activities.ExpenseDetailsActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by ajibigad on 20/08/2017.
 */

public class ErazerFirebaseMessagingService extends FirebaseMessagingService {

    public static final String TAG = ErazerFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i(TAG, "Message From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0 && remoteMessage.getNotification() != null) {
            displayNotification(remoteMessage);
        }
    }

    public void displayNotification(RemoteMessage remoteMessage) {
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        long expenseID = Long.parseLong(remoteMessage.getData().get("id"));
        String title = getString(R.string.new_expense_notification_title,
                notification.getTitleLocalizationArgs()[0]), body = getString(R.string.new_expense_notification_body,
                notification.getBodyLocalizationArgs()[0], notification.getBodyLocalizationArgs()[1]);

        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent expenseDetailIntent = new Intent(this, ExpenseDetailsActivity.class);
        expenseDetailIntent.putExtra(ExpenseDetailsActivity.EXTRA_EXPENSE_ID, expenseID);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, expenseDetailIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setSound(notificationSound)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }
}
