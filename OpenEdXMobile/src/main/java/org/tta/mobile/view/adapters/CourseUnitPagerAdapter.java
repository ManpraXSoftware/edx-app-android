package org.tta.mobile.view.adapters;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.tta.mobile.model.api.EnrolledCoursesResponse;
import org.tta.mobile.model.course.BlockType;
import org.tta.mobile.model.course.CourseComponent;
import org.tta.mobile.model.course.DiscussionBlockModel;
import org.tta.mobile.model.course.HtmlBlockModel;
import org.tta.mobile.model.course.VideoBlockModel;
import org.tta.mobile.util.Config;
import org.tta.mobile.view.CourseUnitDiscussionFragment;
import org.tta.mobile.view.CourseUnitEmptyFragment;
import org.tta.mobile.view.CourseUnitFragment;
import org.tta.mobile.view.CourseUnitMobileNotSupportedFragment;
import org.tta.mobile.view.CourseUnitOnlyOnYoutubeFragment;
import org.tta.mobile.view.CourseUnitVideoFragment;
import org.tta.mobile.view.CourseUnitWebViewFragment;

import java.util.List;

public class CourseUnitPagerAdapter extends FragmentStatePagerAdapter {
    private Config config;
    private List<CourseComponent> unitList;
    private EnrolledCoursesResponse courseData;
    private CourseUnitFragment.HasComponent callback;

    public CourseUnitPagerAdapter(FragmentManager manager,
                                  Config config,
                                  List<CourseComponent> unitList,
                                  EnrolledCoursesResponse courseData,
                                  CourseUnitFragment.HasComponent callback) {
        super(manager);
        this.config = config;
        this.unitList = unitList;
        this.courseData = courseData;
        this.callback = callback;
    }

    public CourseComponent getUnit(int pos) {
        if (pos >= unitList.size())
            pos = unitList.size() - 1;
        if (pos < 0)
            pos = 0;
        return unitList.get(pos);
    }

    /**
     * @return True if given unit is a video unit (not an only on YouTube unit)
     */
    public static boolean isCourseUnitVideo(CourseComponent unit) {
        return (unit instanceof VideoBlockModel && ((VideoBlockModel) unit).getData().encodedVideos.getPreferredVideoInfo() != null);
    }

    @Override
    public Fragment getItem(int pos) {
        CourseComponent unit = getUnit(pos);
        CourseUnitFragment unitFragment;
        //FIXME - for the video, let's ignore studentViewMultiDevice for now
        if (isCourseUnitVideo(unit)) {
            unitFragment = CourseUnitVideoFragment.newInstance((VideoBlockModel) unit, (pos < unitList.size()), (pos > 0));
        } else if (unit instanceof VideoBlockModel && ((VideoBlockModel) unit).getData().encodedVideos.getYoutubeVideoInfo() != null) {
            unitFragment = CourseUnitOnlyOnYoutubeFragment.newInstance(unit);
        } else if (config.isDiscussionsEnabled() && unit instanceof DiscussionBlockModel) {
            unitFragment = CourseUnitDiscussionFragment.newInstance(unit, courseData);
        } else if (!unit.isMultiDevice()) {
            unitFragment = CourseUnitMobileNotSupportedFragment.newInstance(unit);
        } else if (unit.getType() != BlockType.VIDEO &&
                unit.getType() != BlockType.HTML &&
                unit.getType() != BlockType.OTHERS &&
                unit.getType() != BlockType.DISCUSSION &&
                unit.getType() != BlockType.PROBLEM) {
            unitFragment = CourseUnitEmptyFragment.newInstance(unit);
        } else if (unit instanceof HtmlBlockModel) {
            unitFragment = CourseUnitWebViewFragment.newInstance((HtmlBlockModel) unit);
        }

        //fallback
        else {
            unitFragment = CourseUnitMobileNotSupportedFragment.newInstance(unit);
        }

        unitFragment.setHasComponentCallback(callback);

        return unitFragment;
    }

    @Override
    public int getCount() {
        return unitList.size();
    }
}
