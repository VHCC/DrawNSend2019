package ichen.chu.drawnsend.pages.home;

import org.junit.Test;
import org.robolectric.util.FragmentTestUtil;

import ichen.chu.drawnsend.pages.subPage.SubPageEmptyFragment;

import static org.junit.Assert.assertNotNull;

/**
 * Created by IChen.Chu on 2019/11/11
 */
public class HomeFragmentUnitTest {

    @Test
    public void notNull() {
        assertNotNull(HomeFragment.newInstance(500));
    }

}
