package ichen.chu.drawnsend;

import android.util.Log;

public class BusEvent {

    private String message;
    private int mEventType;

    public BusEvent(String mString, int eventType) {
        message = mString;
        mEventType = eventType;
    }

    public String getMessage() {
        return message;
    }

    public int getEventType() {
        return mEventType;
    }
}
