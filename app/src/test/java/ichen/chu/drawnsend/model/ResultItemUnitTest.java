package ichen.chu.drawnsend.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static ichen.chu.drawnsend.model.PlayerItem.TYPE.OWNER;
import static org.junit.Assert.assertEquals;

public class ResultItemUnitTest {

    @Test
    public void resultItemUnitTest () throws JSONException {

        JSONObject userObj = new JSONObject();
        userObj.put("email", "test@gmail.com");
        userObj.put("photoUrl", "photoUrlTest");
        userObj.put("displayName", "displayNameTest");

        ResultItem target = new ResultItem("testResultID", userObj);

        assertEquals("test@gmail.com", target.getUserInfo().getString("email"));
        assertEquals("photoUrlTest", target.getUserInfo().getString("photoUrl"));
        assertEquals("displayNameTest", target.getUserInfo().getString("displayName"));
        assertEquals("testResultID", target.getResultID());
    }

}
