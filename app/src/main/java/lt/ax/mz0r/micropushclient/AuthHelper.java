package lt.ax.mz0r.micropushclient;

import android.util.Base64;

import com.android.volley.AuthFailureError;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by m00n on 1/30/17.
 */

public class AuthHelper {
    static public class Auth {
        String username;
        String password;

        Auth(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public String toString() {
            return String.format("<Auth(%s, %s)>", this.username, this.password);
        }
    }

    static public Map<String, String> getHeaders(Auth auth) throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();

        String credentials = auth.username + ":" + auth.password;
        String httpAuth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        headers.put("Authorization", httpAuth);
        return headers;
    }
}
