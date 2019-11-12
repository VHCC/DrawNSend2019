package ichen.chu.drawnsend.api;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Created by IChen.Chu on 2019/11/12
 */
public class ApiUnitTest {

    private Context mContext;
    private DnsServerAgent.aaaInterface aaa;

    @Before
    public void initTest() {
        mContext = mock(Context.class);
        aaa = new DnsServerAgent.aaaInterface() {
            @Override
            public void onResponse(int code) {
                assertThat(code, equalTo(200));
            }
        };
    }

    @Test
    public void serverStatusTest() {
        DnsServerAgent.getInstance(mContext).getDNSServerStatus();
    }

}
