package org.tta.elementlocators;

public interface IFindCourseLocators_Offline {
	
	String getLNPFindCoursesId();

	String getFindCourseName();

	String getHeaderId();

	public String getOfflineModeTextId();

	public String getOfflineBarId();

	public String getOfflineModeLabelId();
	
	//Android id's
	
	String Android_lnpFindCoursesId="org.tta.mobile:id/drawer_option_find_courses";
	String Android_FindCoursesName="Find Courses";
	String Android_HeaderId="android:id/up";
	
	String Android_OfflineModeTextId="org.tta.mobile:id/offline_mode_message";
	
	String Android_OfflineBarId="org.tta.mobile:id/offline_bar";
	
	String Android_OfflineModeLabelId="org.tta.mobile:id/offline_tv";

}
