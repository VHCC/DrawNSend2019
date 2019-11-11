package ichen.chu.drawnsend.util;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import androidx.core.util.LogWriter;

/**
 * Created by IChen on 2018/08/21.
 */
public class NullHostNameVerifier implements HostnameVerifier {
    private static final MLog Log = new MLog(false);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    @Override
    public boolean verify(String hostname, SSLSession session) {
        Log.i(TAG, "Approving certificate for " + hostname);
        return true;
    }



}
