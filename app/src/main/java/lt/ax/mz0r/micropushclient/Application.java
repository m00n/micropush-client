package lt.ax.mz0r.micropushclient;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by m00n on 12/29/16.
 */

public class Application extends android.app.Application {

    private static final String TAG = "App";
    private BroadcastReceiver messageReceiver;
    private BroadcastReceiver changeReceiver;
    private int unreadNotifications = 0;
    private Map<String, NotificationInfo> notifications;
    private Integer notificationGroupId = 0;
    private Boolean showNotifications = true;

    private class NotificationInfo {
        Integer count = 0;
        Integer notificationId = 0;

        NotificationInfo(int id) {
            this.notificationId = id;
        }

        @Override
        public String toString() {
            return "<NI id=" + notificationId + " count=" + count + ">";
        }
    }

    @Override
    public void onCreate() {
        notifications = new HashMap<>();

        // Create globals singleton from here to ensure context is the app context
        final Globals globals = Globals.getInstance(this.getApplicationContext());
        final PushEntryList messages = globals.getMessages();
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);

        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(
            this.getApplicationContext()
        );

        messages.load();

        long lastRegistration = settings.getLong("last_registration", 0);

        Log.d(TAG, "Last registration: " + lastRegistration);
        if (lastRegistration == 0 || new Date().getTime() - lastRegistration > 86400 * 1000) {
            globals.getApi().sendToken();
        }

        /*
        globals.getApi().syncIcons(new Api.IconSyncCallback() {
            @Override
            public void onIconReceived(Icon icon, Bitmap b) {
                Log.d(TAG, "Icon: " + icon);
            }
        });*/
        globals.getIcons().load();

        messageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Broadcast received " + intent.getExtras());

                /* Icons were updated/changed, resync and don't process push message the
                   usual way
                 */
                if (intent.hasExtra("sync_icons")) {
                    syncIcons();
                    return;
                }

                PushEntry entry = new PushEntry(intent);
                messages.add(entry);

                if (!showNotifications) {
                    return;
                }

                String group = intent.getExtras().getString("group");
                String titleExtra = "";
                NotificationInfo info;
                if (!notifications.containsKey(group)) {
                    notificationGroupId++;
                    info = new NotificationInfo(notificationGroupId);
                    notifications.put(group, info);
                } else {
                    info = notifications.get(group);
                    info.count++;
                    titleExtra = String.format(Locale.getDefault(), " (%1d more)", info.count);
                }

                Log.d(TAG, "Test:" + notifications);
                Bitmap largeIcon = globals.getIcons().get(entry.getIcon());
                NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.default_message_notification_icon)
                        .setLargeIcon(largeIcon)
                        .setContentTitle(entry.getTitle() + titleExtra)
                        .setContentText(entry.getMessage())
                        .setLights(/*0xFFFF00FF*/ Color.MAGENTA, 500, 1000)
                        .setVibrate(new long[] { 0, 750, 125, 750, 125, 250})
                        .setAutoCancel(true)
                        .setWhen(entry.getDate().getTime());

                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                stackBuilder.addNextIntent(resultIntent);

                PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    );
                notificationBuilder.setContentIntent(resultPendingIntent);
                NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(info.notificationId, notificationBuilder.build());
                unreadNotifications++;
            }
        };
        broadcastManager.registerReceiver(
            messageReceiver,
            new IntentFilter("newMessage")
        );

        changeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                messages.save();
            }
        };
        broadcastManager.registerReceiver(
            changeReceiver,
            new IntentFilter(PushEntryList.INTENT_CHANGED)
        );

        broadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show();
                settings.edit().putLong("last_registration", new Date().getTime()).apply();
            }
        }, new IntentFilter(Api.INTENT_REGISTRATION_SUCCESSFUL));

        broadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(context, "Registration failed", Toast.LENGTH_SHORT).show();
            }
        }, new IntentFilter(Api.INTENT_REGISTRATION_FAILED));


        super.onCreate();
    }

    private void syncIcons() {
        Globals globals = Globals.getInstance(getApplicationContext());
        IconMap icons = globals.getIcons();
        icons.clear();
        globals.getApi().syncIcons(
            new IconProcessor(this, icons)
        );
    }

    public void resetNotifications() {
        notifications.clear();
        NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public void setShowNotifications(Boolean showNotifications) {
        this.showNotifications = showNotifications;
    }
}
