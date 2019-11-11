/*
 * Copyright (C) 2017 Samuel Wall
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
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.LOLLIPOP_MR1, manifest = Config.NONE)
//@Config(sdk = Build.VERSION_CODES.O_MR1)
public class ActivityResourceUnitTest {
    @Test
    public void testGetString() {
        final String resource = "DrawNSend";
        final int resourceId = R.string.app_name;
        final MainActivity activity = mock(MainActivity.class);
        when(activity.getString(resourceId)).thenReturn(resource);
        assertEquals(resource, activity.getString(R.string.app_name));
    }

    @Test
    public void testGetDrawable() {

        final Drawable resource = mock(Drawable.class);
        final int resourceId = R.drawable.dns_logo;
        final Activity activity = mock(Activity.class);
        when(activity.getDrawable(resourceId)).thenReturn(resource);
        assertEquals(resource, activity.getDrawable(resourceId));

    }

}
