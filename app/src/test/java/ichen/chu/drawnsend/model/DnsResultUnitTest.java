package ichen.chu.drawnsend.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static ichen.chu.drawnsend.model.PlayerItem.TYPE.OWNER;
import static org.junit.Assert.assertEquals;

public class DnsResultUnitTest {

    @Test
    public void dnsResultUnitTest () throws JSONException {

        DnsResult.getInstance().setResultID("ResultIDTest");
        DnsResult.getInstance().setSubject("SubjectTest");
        DnsResult.getInstance().setFolderID("FolderIDTest");

        assertEquals("ResultIDTest", DnsResult.getInstance().getResultID());
        assertEquals("SubjectTest", DnsResult.getInstance().getSubject());
        assertEquals("FolderIDTest", DnsResult.getInstance().getFolderID());
    }

}
