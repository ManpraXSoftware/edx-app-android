package org.tta.mobile.tta.data.pref;

import android.content.Context;
import android.support.annotation.NonNull;

import org.tta.mobile.module.prefs.PrefManager;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AppPref {

    @NonNull
    private final PrefManager prefManager;

    @Inject
    public AppPref(@NonNull Context context) {
        prefManager = new PrefManager(context, PrefManager.Pref.APP_INFO);
    }

    public void setFirstLaunch(boolean firstLaunch) {
        prefManager.put(PrefManager.Key.FIRST_LAUNCH, firstLaunch);
    }

    public boolean isFirstLaunch() {
        return prefManager.getBoolean(PrefManager.Key.FIRST_LAUNCH, true);
    }

    public boolean isCourseVisited() {
        return prefManager.getBoolean(PrefManager.Key.ISCOURSEVISITED, false);
    }

    public void setCourseVisited(boolean courseVisited) {
        prefManager.put(PrefManager.Key.ISCOURSEVISITED, courseVisited);
    }
    public boolean isCourseBottom() {
        return prefManager.getBoolean(PrefManager.Key.ISCOURSEBOTTOM, false);
    }
    public void setCourseBottom(boolean courseVisited) {
        prefManager.put(PrefManager.Key.ISCOURSEBOTTOM, courseVisited);
    }
    public boolean isProfileVisited() {
        return prefManager.getBoolean(PrefManager.Key.IS_PROFILE_VISITED, false);
    }

    public void setProfileVisited(boolean profileVisited) {
        prefManager.put(PrefManager.Key.IS_PROFILE_VISITED, profileVisited);
    }

    public boolean isSettingVisited() {
        return prefManager.getBoolean(PrefManager.Key.IS_Setting_VISITED, false);
    }
    public void setSettingsVisited(boolean settingsVisited) {
        prefManager.put(PrefManager.Key.IS_Setting_VISITED, settingsVisited);
    }



    public boolean isAgendaVisited() {
        return prefManager.getBoolean(PrefManager.Key.IS_AGENDA_VISITED, false);
    }

    public void setAgendaVisited(boolean agendaVisited) {
        prefManager.put(PrefManager.Key.IS_AGENDA_VISITED, agendaVisited);
    }

    public boolean isFeedVisited() {
        return prefManager.getBoolean(PrefManager.Key.IS_FEED_VISITED, false);
    }

    public void setFeedVisited(boolean feedVisited) {
        prefManager.put(PrefManager.Key.IS_FEED_VISITED, feedVisited);
    }
    public boolean isSearchVisited() {
        return prefManager.getBoolean(PrefManager.Key.IS_SEARCH_VISITED, false);
    }

    public void setSearchVisited(boolean searchVisited) {
        prefManager.put(PrefManager.Key.IS_SEARCH_VISITED, searchVisited);
    }

    public boolean isFeedNavVisited() {
        return prefManager.getBoolean(PrefManager.Key.IS_FEED_NAV_VISITED, false);
    }
    public void setFeedNavVisited(boolean feedNavVisited) {
        prefManager.put(PrefManager.Key.IS_FEED_NAV_VISITED, feedNavVisited);
    }

    public boolean isFirstLogin() {
        return prefManager.getBoolean(PrefManager.Key.FIRST_LOGIN, true);
    }

    public void setCurrentBreadcrumb(String breadcrumb) {
        prefManager.put(PrefManager.Key.CURRENT_BREADCRUMB, breadcrumb);
    }

    public String getCurrentBreadcrumb() {
        String breadcrumb = prefManager.getString(PrefManager.Key.CURRENT_BREADCRUMB);
        return breadcrumb == null ? "" : breadcrumb;
    }
}
