package ichen.chu.drawnsend.api;

import android.content.Context;
import android.os.Handler;

import org.junit.Test;
import org.mockito.Mock;


import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by IChen.Chu on 2019/11/12
 */
public class ApiUnitTest {

    @Mock
    private Context mockContext;

    @Mock
    private Handler mockHandler;

    @Mock
    private DnsServerAgent.ApiTestInterface unitTestInterface = code -> assertThat(code, equalTo(200));


    @Test
    public void serverStatus_Test() {
        DnsServerAgent.getInstance(mockContext)
                .getDNSServerStatus(unitTestInterface);
    }

    @Test
    public void fetchPlayRoomInfo_Test() {
        DnsServerAgent.getInstance(mockContext)
                .fetchPlayRoomInfo(mockHandler, "", unitTestInterface);
    }

    @Test
    public void quitPlayRoom_Test() {
        DnsServerAgent.getInstance(mockContext)
                .quitPlayRoom(mockHandler, "", unitTestInterface);
    }

    @Test
    public void getGameChainFolderID_Test() {
        DnsServerAgent.getInstance(mockContext)
                .getGameChainFolderID(mockHandler, "UnitTest", unitTestInterface);
    }

    @Test
    public void getSubject_Test() {
        DnsServerAgent.getInstance(mockContext)
                .getSubject(mockHandler, 1, true, unitTestInterface);
    }

    @Test
    public void fetchGameChainInfo_Test() {
        DnsServerAgent.getInstance(mockContext)
                .fetchGameChainInfo(mockHandler, "test", unitTestInterface);
    }

    @Test
    public void updateGameChainResult_Test() {
        DnsServerAgent.getInstance(mockContext)
                .updateGameChainResult("test", "test", 1, unitTestInterface);
    }

}
