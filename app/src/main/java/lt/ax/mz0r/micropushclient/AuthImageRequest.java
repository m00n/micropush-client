package lt.ax.mz0r.micropushclient;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by m00n on 1/30/17.
 */

public class AuthImageRequest extends ImageRequest {

    private AuthHelper.Auth auth;

    public AuthImageRequest(String url, AuthHelper.Auth auth, Response.Listener<Bitmap> listener, Response.ErrorListener errorListener) {
        super(url, listener, 0, 0, ImageView.ScaleType.CENTER, Bitmap.Config.ARGB_8888, errorListener);

        this.auth = auth;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return AuthHelper.getHeaders(auth);
    }
}
