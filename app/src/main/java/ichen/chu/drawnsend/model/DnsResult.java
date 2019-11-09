package ichen.chu.drawnsend.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;

public class DnsResult {

    private static DnsResult mDnsResult;

    // field
    private String folderID;
    private String subject;

    private DnsResult() {
    }

    public static DnsResult getInstance() {

        if (null == mDnsResult) {
            mDnsResult = new DnsResult();
        }
        return mDnsResult;
    }

    public String getFolderID() {
        return folderID;
    }

    public void setFolderID(String folderID) {
        this.folderID = folderID;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
