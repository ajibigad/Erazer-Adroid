package com.ajibigad.erazer.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.ajibigad.erazer.R;
import com.ajibigad.erazer.data.Expense;
import com.ajibigad.erazer.network.ExpenseService;
import com.ajibigad.erazer.network.UserService;
import com.ajibigad.erazer.utils.NetworkConnectivityUtils;

import org.parceler.Parcels;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Response;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class ErazerIntentService extends IntentService {

    private static final String ACTION_CREATE_EXPENSE = "com.ajibigad.erazer.service.action.CREATE_EXPENSE";
    private static final String ACTION_CHANGE_STATE = "com.ajibigad.erazer.service.action.CHANGE_STATE";
    private static final String ACTION_UPDATE_FCM_TOKEN = "com.ajibigad.erazer.service.action.UPDATE_FCM_TOKEN";
    private static final String ACTION_DELETE_FCM_TOKEN = "com.ajibigad.erazer.service.action.DELETE_FCM_TOKEN";

    private static final String EXTRA_EXPENSE = "com.ajibigad.erazer.service.extra.expense";
    private static final String EXTRA_STATE = "com.ajibigad.erazer.service.extra.state";
    private static final String EXTRA_EXPENSE_ID = "com.ajibigad.erazer.service.extra.id";
    private static final String EXTRA_FCM_TOKEN = "com.ajibigad.erazer.service.extra.fcm_token";

    private static final String TAG = ErazerIntentService.class.getSimpleName();

    private final Handler mHandler;

    public ErazerIntentService() {
        super("ErazerIntentService");
        mHandler = new Handler();
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionCreateExpense(Context context, Expense expense) {
        Intent intent = new Intent(context, ErazerIntentService.class);
        intent.setAction(ACTION_CREATE_EXPENSE);
        intent.putExtra(EXTRA_EXPENSE, Parcels.wrap(expense));
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionChangeState(Context context, long expenseID, Expense.STATE state) {
        Intent intent = new Intent(context, ErazerIntentService.class);
        intent.setAction(ACTION_CHANGE_STATE);
        intent.putExtra(EXTRA_EXPENSE_ID, expenseID);
        intent.putExtra(EXTRA_STATE, state.name());
        context.startService(intent);
    }

    public static void startActionUpdateFcmToken(Context context, String fcmToken) {
        Intent intent = new Intent(context, ErazerIntentService.class);
        intent.setAction(ACTION_UPDATE_FCM_TOKEN);
        intent.putExtra(EXTRA_FCM_TOKEN, fcmToken);
        context.startService(intent);
    }

    public static void startActionDeleteFcmToken(Context context, String fcmToken) {
        Intent intent = new Intent(context, ErazerIntentService.class);
        intent.setAction(ACTION_DELETE_FCM_TOKEN);
        intent.putExtra(EXTRA_FCM_TOKEN, fcmToken);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CREATE_EXPENSE.equals(action)) {
                final Expense expense = Parcels.unwrap(intent.getParcelableExtra(EXTRA_EXPENSE));
                handleActionCreateExpense(expense);
            } else if (ACTION_CHANGE_STATE.equals(action)) {
                final Expense.STATE state = Expense.STATE.valueOf(intent.getStringExtra(EXTRA_STATE));
                final long expenseID = intent.getLongExtra(EXTRA_EXPENSE_ID, 0);
                handleActionChangeState(expenseID, state);
            } else if (ACTION_UPDATE_FCM_TOKEN.equals(action)) {
                final String fcmToken = intent.getStringExtra(EXTRA_FCM_TOKEN);
                handleActionUpdateFcmToken(fcmToken);
            } else if (ACTION_DELETE_FCM_TOKEN.equals(action)) {
                final String fcmToken = intent.getStringExtra(EXTRA_FCM_TOKEN);
                handleActionDeleteFcmToken(fcmToken);
            }
        }
    }

    private void handleActionUpdateFcmToken(String fcmToken) {
        try {
            Log.i(TAG, "FCM Token: " + fcmToken);
            if (NetworkConnectivityUtils.isConnected(this)) {
                RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), fcmToken);
                Response<Void> response = UserService.getUserClient().addUserFcmToken(requestBody).execute();
                if (!response.isSuccessful()) {
                    //save token in db and leave it to the job to retry this request
                }
            } else {
                //save token in db and leave it to the job to retry this request
            }

        } catch (IOException e) {
            e.printStackTrace();
            //developer should be notified to check this out
        }
    }

    private void handleActionDeleteFcmToken(String fcmToken) {
        try {
            Log.i(TAG, "FCM Token to be deleted: " + fcmToken);
            if (NetworkConnectivityUtils.isConnected(this)) {
                RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), fcmToken);
                Response<Void> response = UserService.getUserClient().deleteUserFcmToken(requestBody).execute();
                if (!response.isSuccessful()) {
                    //save token in db and leave it to the job to retry this request
                }
            } else {
                //save token in db and leave it to the job to retry this request
            }

        } catch (IOException e) {
            e.printStackTrace();
            //developer should be notified to check this out
        }
    }

    /**
     * Handle action create expense in the provided background thread with the provided
     * expense.
     */
    private void handleActionCreateExpense(Expense expense) {
        try {
            if (NetworkConnectivityUtils.isConnected(this)) {
                Response<Expense> response = ExpenseService.getExpenseClient().createExpense(expense).execute();
                if (response.isSuccessful()) {
                    mHandler.post(new RunnableToast(this, "Expense saved successfully"));
                } else {
                    mHandler.post(new RunnableToast(this, "Failed to save expense"));
                }
            } else {
                mHandler.post(new RunnableToast(this, getString(R.string.check_network_connection)));
            }

        } catch (IOException e) {
            e.printStackTrace();
            mHandler.post(new RunnableToast(this, "Error occured while saving expense.\n Pls try again"));
        }
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionChangeState(long expenseID, Expense.STATE state) {
        try {
            if (NetworkConnectivityUtils.isConnected(this)) {
                Response<Expense> response = ExpenseService.getExpenseClient().changeExpenseState(expenseID, state.name()).execute();
                if (response.isSuccessful()) {
                    mHandler.post(new RunnableToast(this, "Expense state updated successfully"));
                } else {
                    mHandler.post(new RunnableToast(this, "Failed to update expense state"));
                }
            } else {
                mHandler.post(new RunnableToast(this, getString(R.string.check_network_connection)));
            }

        } catch (IOException e) {
            e.printStackTrace();
            mHandler.post(new RunnableToast(this, "Error occured while updating expense state.\n Pls try again"));
        }
    }

    protected class RunnableToast implements Runnable {

        Context mContext;
        String message;

        protected RunnableToast(Context context, String message) {
            mContext = context;
            this.message = message;
        }

        @Override
        public void run() {
            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
        }
    }
}
