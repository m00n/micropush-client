package lt.ax.mz0r.micropushclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by m00n on 12/27/16.
 */

public class Api {
    private static final String PREFS_NAME = "micropush";
    private static final String TAG = "api";
    public static final String INTENT_REGISTRATION_SUCCESSFUL = "registrationSuccessful";
    public static final String INTENT_REGISTRATION_FAILED = "registrationFailed";
    private Context context = null;
    private RequestQueue queue;
    private AuthHelper.Auth auth;
    private final SharedPreferences settings;
    private boolean registered = false;

    public interface IconSyncCallback {
        public void onIconReceived(Icon icon, Bitmap b);
    }

    public interface FullMessageCallback {
        public void onFullMessageReceived(String text);
        public void onError(VolleyError e);
    }

    Api(Context context) {
        this.context = context;
        this.settings = PreferenceManager.getDefaultSharedPreferences(context);
        this.setAuth();
        Log.d(TAG, "Using auth " + this.auth);
        this.registered = settings.getBoolean("is_registered", false);
        this.queue = Volley.newRequestQueue(context);
    }

    public void setAuth() {
        this.auth = new AuthHelper.Auth(
            settings.getString("username", null),
            settings.getString("password", null)
        );
    }

    public Boolean isConfigured() {
        return settings.getString("api_url", null) != null;
    }

    private String getApiEndpoint(String endpoint) {
        /*
        String apiUrl = settings.getString("api_url", null);
        Uri.Builder builder = Uri.parse(apiUrl)
                .buildUpon()
                .appendPath("api")
                .appendPath("v1")
                .appendPath(endpoint);

        return builder.toString();*/
        return getApiEndpoint(new String[] {endpoint});
    }

    private String getApiEndpoint(String[] parts) {
        String apiUrl = settings.getString("api_url", null);
        Uri.Builder builder = Uri.parse(apiUrl)
                .buildUpon()
                .appendPath("api")
                .appendPath("v1");

        for (String endpoint : parts) {
            builder.appendEncodedPath(endpoint);
        }

        return builder.toString();
    }

    private JSONObject makeJSONObject() {
        String uuid = FirebaseInstanceId.getInstance().getId();
        JSONObject json = new JSONObject();
        try {
            json.put("uuid", uuid);
        } catch (JSONException e) {
            return null;
        }

        return json;
    }

    private void broadcast(Intent intent) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void verify() {
        JSONObject json = makeJSONObject();

        JsonObjectRequest req = new AuthJsonObjectRequest(Request.Method.GET, getApiEndpoint("verify"), auth, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Intent intent = new Intent("verfiyResults");

                try {
                    intent.putExtra("isKnownDevice", response.getBoolean("known_device"));
                } catch (JSONException e) {
                    throw new RuntimeExecutionException(e);
                }
                broadcast(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Intent intent = new Intent("verifyError");
                broadcast(intent);
            }
        });

        queue.add(req);
    }

    public boolean sendToken() {
        if (!isConfigured())
            return false;

        String token = FirebaseInstanceId.getInstance().getToken();
        String uuid = FirebaseInstanceId.getInstance().getId();
        String name = android.os.Build.MODEL;

        JSONObject json = new JSONObject();
        try {
            json.put("token", token);
            json.put("uuid", uuid);
            json.put("name", name);
        } catch (JSONException e) {
            throw new RuntimeExecutionException(e);
        }

        if (!settings.contains("api_url")) {
            Intent intent = new Intent(INTENT_REGISTRATION_FAILED);
            intent.putExtra("reason", "No API endpoint supplied");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            return false;
        }

        JsonObjectRequest req = new AuthJsonObjectRequest(Request.Method.POST, getApiEndpoint("register"), auth, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                settings.edit().putBoolean("is_registered", true).apply();
                Log.d(TAG, "Registration successful");

                Intent intent = new Intent(INTENT_REGISTRATION_SUCCESSFUL);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration failed", error);

                Intent intent = new Intent(INTENT_REGISTRATION_FAILED);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });

        queue.add(req);

        return true;
    }

    void getFullMessage(Integer messageId, final FullMessageCallback callback) {
        String endpoint = getApiEndpoint(new String[] {"messages", messageId.toString(), "text"});
        JSONObject json = new JSONObject();
        Log.d(TAG, "getFullMessage " + endpoint);
        JsonObjectRequest req = new AuthJsonObjectRequest(Request.Method.GET, endpoint, auth, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    callback.onFullMessageReceived(response.getString("message"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(error);
            }
        });

        queue.add(req);
    }

    void syncIcons(final IconSyncCallback callback) {
        if (!isConfigured())
            return;

        JSONObject json = new JSONObject();

        JsonObjectRequest req = new AuthJsonObjectRequest(Request.Method.GET, getApiEndpoint("icons/"), auth, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Iterator iterator = response.keys();
                while (iterator.hasNext()) {
                    Icon icon;
                    String key = (String) iterator.next();
                    Log.d(TAG, "syncIcons: " + key);
                    try {
                        JSONObject iconData = response.getJSONObject(key);
                        Integer id = iconData.getInt("icon_id");
                        String url = iconData.getString("url");
                        String name = iconData.getString("name");
                        icon = new Icon(id, name, url);

                    } catch (JSONException e) {
                        // This is beyond retarded, but lets wrap the exception and move the fuck on
                        throw new RuntimeException(e);
                    }
                    syncIcon(icon, callback);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "", error);
            }
        });

        queue.add(req);
    }

    private void syncIcon(final Icon icon, final IconSyncCallback callback) {
        ImageRequest req = new AuthImageRequest(icon.getUrl(), auth, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                callback.onIconReceived(icon, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(req);
    }

}
