package lt.ax.mz0r.micropushclient;

import android.provider.SyncStateContract;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class AuthJsonObjectRequest extends JsonObjectRequest {
    private AuthHelper.Auth auth;

    public AuthJsonObjectRequest(int method, String url, AuthHelper.Auth auth, JSONObject data, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, data, listener, errorListener);

        this.auth = auth;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return AuthHelper.getHeaders(auth);
    }
}
