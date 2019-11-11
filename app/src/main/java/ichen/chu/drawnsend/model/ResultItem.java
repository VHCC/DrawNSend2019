package ichen.chu.drawnsend.model;

import org.json.JSONObject;

/**
 * Created by IChen.Chu on 2019/11/04
 */
public class ResultItem {

    String mResultID;

    JSONObject userInfo;

    public ResultItem(String resultsID, JSONObject user_Info) {
        this.mResultID = resultsID;
        this.userInfo = user_Info;
    }

    public String getResultID() {
        return mResultID;
    }

    public JSONObject getUserInfo() {
        return userInfo;
    }

    // --------------------- category --------------

    @Override
    public String toString() {
        return "result, resultID= " + mResultID +
                ", userInfo= " + userInfo;
    }
}
