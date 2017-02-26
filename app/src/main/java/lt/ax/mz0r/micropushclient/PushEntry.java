package lt.ax.mz0r.micropushclient;

import android.content.Intent;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by m00n on 12/21/16.
 */

public class PushEntry implements Serializable {

    private Integer id = 0;
    private String title = "";
    private Integer icon = 0;
    private String message = "";
    private String url = "";
    private Date date;
    private Boolean fullMessageAvailable = false;

    PushEntry(String title, String message) {
        this.title = title;
        this.message = message;
        this.date = new Date(0);
    }

    PushEntry(Intent intent) {
        this.title = intent.getStringExtra("title");
        this.message = intent.getStringExtra("message");
        this.date = new Date(intent.getLongExtra("timestamp", 0L) * 1000);
        this.id = intent.getIntExtra("notification_id", 0);
        this.fullMessageAvailable = intent.getBooleanExtra("full_message", false);
        this.icon = intent.getIntExtra("icon", 0);
    }

    @Override
    public String toString() {
        return this.title + "\n" + this.message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getId() {
        return id;
    }

    public Boolean hasFullMessage() {
        return fullMessageAvailable;
    }

    public Integer getIcon() {
        return icon;
    }
}
