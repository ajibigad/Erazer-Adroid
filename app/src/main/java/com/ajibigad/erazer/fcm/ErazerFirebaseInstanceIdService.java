package com.ajibigad.erazer.fcm;

import android.content.Context;
import android.util.Log;

import com.ajibigad.erazer.network.UserService;
import com.ajibigad.erazer.service.ErazerIntentService;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by ajibigad on 15/08/2017.
 */

public class ErazerFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static String TAG = ErazerFirebaseInstanceIdService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.i(TAG, "Refreshed FCM token: " + refreshedToken);
        if (UserService.getCurrentUser() != null) {
            getTokenAndUpdateServer(this);
        }
    }

    public static void getTokenAndUpdateServer(Context context) {
        // Get updated InstanceID token.
        String fcmToken = FirebaseInstanceId.getInstance().getToken();
        Log.i(TAG, "FCM Token: " + fcmToken);

        ErazerIntentService.startActionUpdateFcmToken(context, fcmToken);
    }
}
