package ichen.chu.drawnsend.pages.subPage;

import org.junit.Rule;
import org.junit.Test;

import androidx.test.platform.app.InstrumentationRegistry;
import ichen.chu.drawnsend.MainActivity;
import ichen.chu.drawnsend.model.DnsResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

/**
 * Created by IChen.Chu on 2019/11/11
 */
public class SubPageEmptyFragmentUnitTest {

    @Test
    public void notNull() {
        assertNotNull(SubPageEmptyFragment.newInstance());
    }
}
