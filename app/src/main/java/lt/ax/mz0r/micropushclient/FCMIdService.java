package lt.ax.mz0r.micropushclient;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;

public class FCMIdService extends FirebaseInstanceIdService {
    private static final String TAG = "FCMId";

    public FCMIdService() {
        super();
    }

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        Globals.getInstance(this.getApplicationContext()).getApi().sendToken();
    }
}
