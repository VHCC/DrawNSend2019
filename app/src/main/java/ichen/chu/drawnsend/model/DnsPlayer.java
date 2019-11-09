package ichen.chu.drawnsend.model;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DnsPlayer {

    static DnsPlayer mDnsPlayer;

    private DnsPlayer() {
    }

    private JSONObject playInfoObject;
    private JSONArray playerOrders;

    public static DnsPlayer getInstance() {

        if (null == mDnsPlayer) {
            mDnsPlayer = new DnsPlayer();
        }
        return mDnsPlayer;
    }

    public void setPlayerInfo(GoogleSignInAccount acct) {
        playInfoObject = new JSONObject();
        try {
            playInfoObject.put("email", acct.getEmail());
            playInfoObject.put("displayName", acct.getDisplayName());
            playInfoObject.put("photoUrl", acct.getPhotoUrl());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getPlayInfoObject () {
        return playInfoObject;
    }

    public void setOrders(JSONArray array) {
        playerOrders = array;
    }

    public JSONArray getPlayerOrders() {
        return playerOrders;
    }
}
