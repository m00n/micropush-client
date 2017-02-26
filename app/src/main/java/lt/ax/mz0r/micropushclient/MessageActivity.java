package lt.ax.mz0r.micropushclient;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;

public class MessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar actionBar = (Toolbar) findViewById(R.id.ma_toolbar);
        actionBar.setTitle("µpush");
        setSupportActionBar(actionBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(0xff0099cc); // FIXME
        }

        String fullMessage = getIntent().getStringExtra("full_message");
        WebView view = (WebView) findViewById(R.id.webview);
        view.loadData(fullMessage, "text/html; charset=utf-8", "utf-8");
    }
}
