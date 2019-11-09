package ichen.chu.drawnsend.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;

public class DnsPlayRoom {

    private static DnsPlayRoom mDnsPlayRoom;

    private final String OWNER = "roomOwner";
    private final String PARTICIPANTS = "participants";
    private final String PLAYORDERS = "playOrders";
    private final String ISADULT = "isAdult";
    private final String ROOMSTATUS = "roomStatus";
    private final String PLAYTIME = "playTime";
    private final String DIFFICULTY = "difficulty";
    private final String JOINNUMBER = "joinNumber";

    // field
    private JSONObject roomOwner;
    private int playTime;
    private String joinNumber;
    private JSONArray participants;
    private JSONArray playOrders;
    private int difficulty;
    private boolean isAdult;
    private int roomStatus;

    private DnsPlayRoom() {
    }

    public static DnsPlayRoom getInstance() {

        if (null == mDnsPlayRoom) {
            mDnsPlayRoom = new DnsPlayRoom();
        }
        return mDnsPlayRoom;
    }

    public void setRoomInfo(JSONObject roomInfoObj) {
        try {
            roomOwner = (JSONObject) roomInfoObj.getJSONArray(OWNER).get(0);
            participants = roomInfoObj.getJSONArray(PARTICIPANTS);
            joinNumber = roomInfoObj.getString(JOINNUMBER);
            playTime = roomInfoObj.getInt(PLAYTIME);
            playOrders = roomInfoObj.getJSONArray(PLAYORDERS);
            difficulty = roomInfoObj.getInt(DIFFICULTY);
            roomStatus = roomInfoObj.getInt(ROOMSTATUS);
            isAdult = roomInfoObj.getBoolean(ISADULT);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public JSONObject getRoomOwner() {
        return roomOwner;
    }

    public int getPlayTime() {
        return playTime;
    }

    public String getJoinNumber() {
        return joinNumber;
    }

    public JSONArray getParticipants() {
        return participants;
    }

    public JSONArray getPlayOrders() {
        return playOrders;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public boolean isAdult() {
        return isAdult;
    }

    public int getRoomStatus() {
        return roomStatus;
    }

    @NonNull
    @Override
    public String toString() {
        return "roomInfo" +
                ", roomOwner= " + roomOwner +
                ", playTime= " + playTime +
                ", joinNumber= " + joinNumber +
                ", participants= " + participants +
                ", playOrders= " + playOrders +
                ", difficulty= " + difficulty +
                ", isAdult= " + isAdult +
                ", roomStatus= " + roomStatus;

    }
}
