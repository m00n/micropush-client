package lt.ax.mz0r.micropushclient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;

/**
 * Created by m00n on 2/15/17.
 */

public class Icon implements Serializable {
    private Integer id;
    private String url;
    private String name;

    Icon(Integer id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }

    public Bitmap load(Context context) {
        return BitmapFactory.decodeFile(context.getCacheDir() + "/" + id + ".processed.png");
    }

    public String getUrl() {
        return url;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public String toString() {
        return "<Icon id=" + id + " url=" + url + ">";
    }

    public String getName() {
        return name;
    }
}
