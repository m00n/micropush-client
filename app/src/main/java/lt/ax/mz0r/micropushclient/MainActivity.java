package lt.ax.mz0r.micropushclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "micropush";
    private static final String TAG = "main";
    private PushEntryList messages;
    private String fcmId = "";
    private Api api = null;
    private BroadcastReceiver changeReceiver = null;
    private PushEntryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Application app = (Application) getApplication();
        app.resetNotifications();

        final Globals globals = Globals.getInstance(getApplicationContext());

        messages = globals.getMessages();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle("µpush");
        setSupportActionBar(myToolbar);

        api = globals.getApi();

        adapter = new PushEntryAdapter(
            getApplicationContext(),
            messages.getList(),
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    PushEntry entry = messages.getList().get(position);
                    Log.d(TAG, "onclick "+ position + " " + entry.hasFullMessage());
                    if (entry.hasFullMessage()) {
                        globals.getApi().getFullMessage(entry.getId(), new Api.FullMessageCallback() {
                            @Override
                            public void onFullMessageReceived(String text) {
                                Log.d(TAG, "Full message: " + text);
                                Intent intent = new Intent(MainActivity.this, MessageActivity.class);
                                intent.putExtra("full_message", text);
                                startActivity(intent);
                            }

                            @Override
                            public void onError(VolleyError e) {
                                Toast.makeText(MainActivity.this, "Couldn't load full message", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(0xff0099cc); // FIXME
        }


        final RecyclerView listView = (RecyclerView) findViewById(R.id.pushMessagesView);
        listView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(adapter);
        //TextView empty = (TextView) findViewById(R.id.emptyText);
        //listView.setEmptyView(empty);
        listView.scrollToPosition(0);
        listView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick");
                //globals.getApi().getFullMessage();
                //startActivity();
            }
        });

        registerForContextMenu(listView);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                adapter.enableAnimations();
            }
        }, 1000);

        changeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                adapter.notifyDataSetChanged();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(
            changeReceiver,
            new IntentFilter(PushEntryList.INTENT_CHANGED)
        );

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                listView.scrollToPosition(0);
            }
        }, new IntentFilter(FCMService.INTENT_NEW_MESSAGE));

        ((Application) getApplication()).setShowNotifications(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.actionRefresh:
                Toast.makeText(this, "Registering...", Toast.LENGTH_SHORT).show();
                Globals.getInstance(getApplicationContext()).getApi().sendToken();
                Globals.getInstance(getApplicationContext()).getApi().syncIcons(new IconProcessor(this, Globals.getInstance(getApplicationContext()).getIcons()));
                return true;

            case R.id.actionSettings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.actionClear:
                messages.clear();
                return true;

            case R.id.actionFakeData:
                for (int i = 0; i < 20; i++) {
                    messages.add(new PushEntry("Hello", "Hello World!" + i));
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v.getId() == R.id.pushMessagesView) {
            getMenuInflater().inflate(R.menu.menu_list_context, menu);
        }
    }*/


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Integer position = adapter.getPosition();
        switch (item.getItemId()) {
            case R.id.actionDeleteEntry:
                messages.delete(position);
                return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ((Application) getApplication()).setShowNotifications(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((Application) getApplication()).setShowNotifications(true);
    }
}
