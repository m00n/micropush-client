package lt.ax.mz0r.micropushclient;

import android.content.Context;

/**
 * Created by m00n on 12/28/16.
 */

public class State {



    private enum States {
        STARTED, VERIFIED, REGISTERED
    }

    private States state = States.STARTED;
    private Context context = null;
    private Api api;

    State(Context context) {
        this.context = context;
        this.api = new Api(context);
        handle();
    }

    void handle() {
        switch (state) {
            case STARTED:
                api.verify();
                return;
            case VERIFIED:
                api.sendToken();
                return;
            case REGISTERED:
                return;
        }
    }

    void setState(States state) {
        this.state = state;
        handle();
    }

}
