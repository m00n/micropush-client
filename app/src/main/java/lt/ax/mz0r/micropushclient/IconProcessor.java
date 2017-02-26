package lt.ax.mz0r.micropushclient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by m00n on 1/30/17.
 */

public class IconProcessor implements Api.IconSyncCallback {

    Context context;
    IconMap icons;

    IconProcessor(Context context, IconMap icons) {
        this.context = context;
        this.icons = icons;
    }

    @Override
    public void onIconReceived(Icon icon, Bitmap bitmap) {
        Bitmap background = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
            context.getResources(),
            R.drawable.tmp
        ), 256, 256, true);
        Bitmap iconBitmap = background.copy(Bitmap.Config.ARGB_8888, true);

        if (bitmap.getHeight() > 256 || bitmap.getWidth() > 256) {
            bitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, false);
        }
        Log.d("ICONPROC", "b " + background.getWidth() + "x" + background.getHeight());
        Canvas canvas = new Canvas(iconBitmap);
        int x = (background.getWidth() / 2) - (bitmap.getWidth() / 2);
        int y = (background.getHeight() / 2) - (bitmap.getHeight() / 2);
        canvas.drawBitmap(bitmap, x, y, null);

        try {
            FileOutputStream fos = getOutputStream(icon.getId(), "raw");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            fos = getOutputStream(icon.getId(), "processed");
            iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        icons.add(icon);
        icons.save();
    }

    FileOutputStream getOutputStream(int id, String suffix) throws FileNotFoundException {
        return new FileOutputStream(
            new File(
                context.getCacheDir(),
                id + "." + suffix + ".png"
            )
        );
    }
}
