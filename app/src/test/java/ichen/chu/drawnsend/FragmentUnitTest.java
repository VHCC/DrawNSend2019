/*
 * Copyright (C) 2017-2019 Samuel Wall
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ichen.chu.drawnsend;

import android.app.Activity;
import android.os.Build;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;


import androidx.annotation.NonNull;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import ichen.chu.drawnsend.pages.dashboard.DashboardMainFragment;
import ichen.chu.drawnsend.pages.results.ResultsFragment;
import ichen.chu.drawnsend.pages.subPage.SubPageEmptyFragment;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
public class FragmentUnitTest {


    @Before
    public void initTest() {
        final Activity activity = mock(Activity.class);
    }

    @Test
    public void testInstanceFragment() {
        FragmentScenario<SubPageEmptyFragment> scenario = FragmentScenario.launchInContainer(SubPageEmptyFragment.class);
        scenario.onFragment(new FragmentScenario.FragmentAction<SubPageEmptyFragment>() {
            @Override
            public void perform(@NonNull SubPageEmptyFragment fragment) {
                assertTrue(fragment instanceof SubPageEmptyFragment);
            }
        });

        FragmentScenario<ResultsFragment> scenario3 = FragmentScenario.launch(ResultsFragment.class);
        scenario3.onFragment(new FragmentScenario.FragmentAction<ResultsFragment>() {
            @Override
            public void perform(@NonNull ResultsFragment fragment) {
                assertTrue(fragment instanceof ResultsFragment);
            }
        });


        FragmentScenario<DashboardMainFragment> scenario4 = FragmentScenario.launch(DashboardMainFragment.class);
        scenario4.onFragment(new FragmentScenario.FragmentAction<DashboardMainFragment>() {
            @Override
            public void perform(@NonNull DashboardMainFragment fragment) {
                assertTrue(fragment instanceof DashboardMainFragment);
            }
        });

    }

}
