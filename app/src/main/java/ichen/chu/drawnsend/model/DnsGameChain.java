package ichen.chu.drawnsend.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;

public class DnsGameChain {

    private static DnsGameChain mDnsGameChain;

    private final String PLAYERCHAINED = "playerChained";
    private final String RESULTSCHAINED = "resultsChained";
    private final String CHAINID = "chainID";
    private final String CREATOR = "creator";
    private final String PARENTID = "parentID";
    private final String SUBJECT = "subject";

    // field
    private JSONArray playerChained;
    private JSONArray resultsChained;
    private String chainID;
    private String creator;
    private String parentID;
    private String subject;

    private DnsGameChain() {
    }

    public static DnsGameChain getInstance() {

        if (null == mDnsGameChain) {
            mDnsGameChain = new DnsGameChain();
        }
        return mDnsGameChain;
    }

    public void setGameChainInfo(JSONObject gameChainInfo) {
        try {
            playerChained = gameChainInfo.getJSONArray(PLAYERCHAINED);
            resultsChained = gameChainInfo.getJSONArray(RESULTSCHAINED);
            chainID = gameChainInfo.getString(CHAINID);
            creator = gameChainInfo.getString(CREATOR);
            parentID = gameChainInfo.getString(PARENTID);
            subject = gameChainInfo.getString(SUBJECT);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONArray getPlayerChained() {
        return playerChained;
    }

    public JSONArray getResultsChained() {
        return resultsChained;
    }

    public String getChainID() {
        return chainID;
    }

    public String getCreator() {
        return creator;
    }

    public String getParentID() {
        return parentID;
    }

    public String getSubject() {
        return subject;
    }

    @NonNull
    @Override
    public String toString() {
        return "Game Chain Info" +
                ", subject= " + subject +
                ", chainID= " + chainID +
                ", creator= " + creator +
                ", parentID= " + parentID +
                ", playerChained size= " + playerChained.length() +
                ", resultsChained size= " + resultsChained.length();

    }
}
