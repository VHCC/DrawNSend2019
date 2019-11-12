package ichen.chu.drawnsend.pages.dashboard;

import org.junit.Test;

import ichen.chu.drawnsend.pages.subPage.SubPageEmptyFragment;

import static org.junit.Assert.assertNotNull;

/**
 * Created by IChen.Chu on 2019/11/11
 */
public class DashboardFragmentUnitTest {

    @Test
    public void notNull() {
        assertNotNull(DashboardMainFragment.newInstance());
    }
}
