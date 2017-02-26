package lt.ax.mz0r.micropushclient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by m00n on 2/16/17.
 */

public class IconMap {
    private static final String CACHEFILE = "icons.dat";
    private Context context;
    private Map<Integer, Icon> icons;

    IconMap(Context context) {
        this.context = context;
        this.icons = new HashMap<>();
    }

    public Map<Integer, Icon> getMap() {
        return icons;
    }

    public void clear() {
        icons.clear();
    }

    public void add(Icon icon) {
        icons.put(icon.getId(), icon);
    }

    public Bitmap get(Integer id) {
        Icon icon = icons.get(id);
        if (icon != null) {
            return icon.load(context);
        } else {
            return Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(
                    context.getResources(),
                    R.drawable.default_message_icon
                ),
                256, 256, true
            );
        }
    }

    public void save() {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(new File(context.getCacheDir(), CACHEFILE));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(fos);
            outputStream.writeObject(icons);
        } catch (IOException e) {
            // Just let the app die properly...
            throw new RuntimeException(e);
        }
    }

    public void load() {
        FileInputStream fis;
        try {
            fis = new FileInputStream(new File(context.getCacheDir(), CACHEFILE));
        } catch (FileNotFoundException e) {
            return;
        }

        try {
            ObjectInputStream inputStream = new ObjectInputStream(fis);
            icons.putAll((Map<Integer, Icon>) inputStream.readObject());
        } catch (IOException | ClassNotFoundException e) {
            return;
        }
    }
}
