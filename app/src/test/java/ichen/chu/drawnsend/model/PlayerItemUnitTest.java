package ichen.chu.drawnsend.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static ichen.chu.drawnsend.model.PlayerItem.TYPE.OWNER;
import static org.junit.Assert.assertEquals;

public class PlayerItemUnitTest {

    @Test
    public void playerItemUnitTest () throws JSONException {
        JSONObject userObj = new JSONObject();
        userObj.put("email", "test@gmail.com");
        userObj.put("photoUrl", "photoUrlTest");
        userObj.put("displayName", "displayNameTest");

        PlayerItem target = new PlayerItem(OWNER, userObj);

        assertEquals("test@gmail.com", target.getUserInfo().getString("email"));
        assertEquals("photoUrlTest", target.getUserInfo().getString("photoUrl"));
        assertEquals("displayNameTest", target.getUserInfo().getString("displayName"));
        assertEquals(OWNER, target.getPlayerType());
    }

}
