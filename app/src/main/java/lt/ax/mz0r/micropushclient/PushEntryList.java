package lt.ax.mz0r.micropushclient;


import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.tasks.RuntimeExecutionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by m00n on 12/29/16.
 */

public class PushEntryList {
    public static String INTENT_CHANGED = "messagesChanged";
    private static final String CACHEFILE = "messages.dat";
    private Context context;
    private ArrayList<PushEntry> entries;

    PushEntryList(Context context) {
        this.entries = new ArrayList<>();
        this.context = context;
    }

    public List<PushEntry> getList() {
        return entries;
    }

    public void save() {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(new File(this.context.getCacheDir(), CACHEFILE));
        } catch (FileNotFoundException e) {
            // Just rethrow. Thanks checked exceptions!
            throw new RuntimeException(e);
        }

        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(fos);
            outputStream.writeObject(entries);
        } catch (IOException e) {
            // Just let the app die properly...
            throw new RuntimeException(e);
        }
    }

    public void load() {
        FileInputStream fis;
        try {
            fis = new FileInputStream(new File(this.context.getCacheDir(), CACHEFILE));
        } catch (FileNotFoundException e) {
            return;
        }

        try {
            ObjectInputStream inputStream = new ObjectInputStream(fis);
            this.entries.addAll((List<PushEntry>) inputStream.readObject());
        } catch (InvalidClassException e) {
            // PushEntry class changed. Nothing we can do...
            return;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void add(PushEntry entry) {
        entries.add(0, entry);
        broadcastChange();
    }

    public void delete(int position) {
        entries.remove(position);
        broadcastChange();
    }

    public void clear() {
        entries.clear();
        broadcastChange();
    }

    private void broadcastChange() {
        Intent intent = new Intent(INTENT_CHANGED);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }
}
