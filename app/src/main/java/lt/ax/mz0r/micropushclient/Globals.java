package lt.ax.mz0r.micropushclient;

import android.content.Context;


/**
 * Created by m00n on 12/28/16.
 */

public class Globals {
    private static Globals instance = null;

    private Context context;
    private Api api;
    private PushEntryList messages;
    private IconMap icons;

    private Globals(Context context) {
        this.context = context;
        this.api = new Api(context);
        this.messages = new PushEntryList(context);
        this.icons = new IconMap(context);
    }

    static Globals getInstance(Context context) {
        if (instance == null) {
            instance = new Globals(context);
        }

        return instance;
    }

    PushEntryList getMessages() {
        return messages;
    }

    IconMap getIcons() {
        return icons;
    }

    Api getApi() {
        return api;
    }


}
