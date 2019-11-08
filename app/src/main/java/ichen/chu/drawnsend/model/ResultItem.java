package ichen.chu.drawnsend.model;

import org.json.JSONObject;

/**
 * Created by IChen.Chu on 2019/11/04
 */
public class ResultItem {

    String mResultsUrl;

    JSONObject userInfo;

    public ResultItem(String resultsUrl, JSONObject user_Info) {
        this.mResultsUrl = resultsUrl;
        this.userInfo = user_Info;
    }

    public String getResultsUrl() {
        return mResultsUrl;
    }

    public JSONObject getUserInfo() {
        return userInfo;
    }

    // --------------------- category --------------


    @Override
    public String toString() {

        return "result= " + mResultsUrl;
    }
}
