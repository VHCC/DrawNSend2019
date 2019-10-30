package ichen.chu.drawnsend;

import android.util.Log;

public class BusEvent {

    private String message;

    public BusEvent (String mString) {
        message = mString;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
