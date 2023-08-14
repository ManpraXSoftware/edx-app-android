package org.humana.mobile.test.feature.interactor;

import org.humana.mobile.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.humana.mobile.test.feature.matcher.ActionBarMatcher.isInActionBar;
import static org.hamcrest.CoreMatchers.allOf;

public class NavigationDrawerInteractor {
    public static NavigationDrawerInteractor open() {
        onView(allOf(isInActionBar(), withContentDescription("dummy string"))).perform(click());
        return new NavigationDrawerInteractor();
    }

    public LogInScreenInteractor logOut() {
        onView(withText(R.string.logout)).perform(click());
        return new LogInScreenInteractor();
    }
}
