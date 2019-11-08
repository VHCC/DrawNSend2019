package ichen.chu.drawnsend.model;

import org.json.JSONObject;

/**
 * Created by IChen.Chu on 2019/11/04
 */
public class PlayerItem {

    public enum TYPE {
        OWNER, PARTICIPANTS,
        OWNER_RESULTS, PARTICIPANTS_RESULTS,
    }

    TYPE playerType;
    JSONObject userInfo;

    final String wrongType = "wrong type";


    public PlayerItem(TYPE player_type, JSONObject user_Info) {
        this.playerType = player_type;
        this.userInfo = user_Info;
    }

    public TYPE getPlayerType() {
        return playerType;
    }

    public JSONObject getUserInfo() {
        return userInfo;
    }

// --------------------- category --------------



    @Override
    public String toString() {

        return "TYPE= " + playerType +
                ", userJson= " + userInfo;
    }
}
