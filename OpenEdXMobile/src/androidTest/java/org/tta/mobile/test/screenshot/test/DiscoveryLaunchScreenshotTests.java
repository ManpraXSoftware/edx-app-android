package org.tta.mobile.test.screenshot.test;

import org.tta.mobile.view.DiscoveryLaunchActivity;
import org.tta.mobile.view.DiscoveryLaunchPresenter;
import org.tta.mobile.view.PresenterActivityScreenshotTest;
import org.junit.Test;

public class DiscoveryLaunchScreenshotTests extends PresenterActivityScreenshotTest<DiscoveryLaunchActivity, DiscoveryLaunchPresenter, DiscoveryLaunchPresenter.ViewInterface> {

    @Test
    public void testScreenshot_withCourseDiscoveryDisabled() {
        view.setEnabledButtons(false);
    }

    @Test
    public void testScreenshot_withCourseDiscoveryEnabled() {
        view.setEnabledButtons(true);
    }
}
