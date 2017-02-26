package lt.ax.mz0r.micropushclient;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class FCMService extends FirebaseMessagingService {
    private static final String TAG = "FCM";
    public static final String INTENT_NEW_MESSAGE = "newMessage";

    public FCMService() {
        super();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> payload = remoteMessage.getData();
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }
        Intent intent = new Intent("newMessage");
        intent.putExtra("title", payload.get("title"));
        intent.putExtra("message", payload.get("message"));
        intent.putExtra("timestamp", Long.valueOf(payload.get("timestamp")));
        intent.putExtra("group", payload.get("group"));
        intent.putExtra("full_message", Boolean.valueOf(payload.get("full_message")));
        intent.putExtra("notification_id", Integer.valueOf(payload.get("notification_id")));
        intent.putExtra("icon", Integer.valueOf(payload.get("icon")));
        Long id = 0L;
        if (payload.containsKey("id")) {
            id = Long.valueOf(payload.get("id"));
        }
        intent.putExtra("id", id);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
