package ichen.chu.drawnsend;

import android.app.Activity;
import android.os.RemoteException;
import android.util.Log;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import androidx.test.filters.LargeTest;
import androidx.test.filters.RequiresDevice;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import ichen.chu.drawnsend.util.MLog;

import static org.junit.Assert.assertNotNull;

/**
 * Created by IChen.Chu on 2019/11/11
 */
public class MainActivityInstTest {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());


    @Rule
    public ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    @RequiresDevice
    public void testRequiresDevice() {
        mLog.d(TAG, "This test requires a device");
        Activity activity = activityTestRule.getActivity();
        assertNotNull("MainActivity is not available", activity);
    }

    @Test
    @SmallTest
    public void testSmallTest() {
        mLog.d(TAG, "this is a small test");
        Activity activity = activityTestRule.getActivity();
        assertNotNull("MainActivity is not available", activity);
    }

    @Test
    @LargeTest
    public void testLargeTest() {
        mLog.d(TAG, "This is a large test");
        Activity activity = activityTestRule.getActivity();
        assertNotNull("MainActivity is not available", activity);
    }

    @Test
    public void testPressBackButton() {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack();
    }

    @Test
    public void testUiDevice() throws RemoteException {
        UiDevice device = UiDevice.getInstance(
                InstrumentationRegistry.getInstrumentation());
        if (device.isScreenOn()) {
            device.setOrientationLeft();
            device.openNotification();
        }
    }

    @Ignore
    @Test
    public void testUiAutomatorAPI() throws UiObjectNotFoundException, InterruptedException {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        UiSelector editTextSelector = new UiSelector().className("android.widget.EditText").text("this is a test").focusable(true);
        UiObject editTextWidget = device.findObject(editTextSelector);
        editTextWidget.setText("this is new text");

        Thread.sleep(2000);

        UiSelector buttonSelector = new UiSelector().className("android.widget.Button").text("CLICK ME").clickable(true);
        UiObject buttonWidget = device.findObject(buttonSelector);
        buttonWidget.click();

        Thread.sleep(2000);
    }

}
