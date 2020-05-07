package org.tta.mobile.tta.ui.library.view_model;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;

import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import org.tta.mobile.logger.Logger;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.data.local.db.table.Category;
import org.tta.mobile.tta.data.model.library.CollectionConfigResponse;
import org.tta.mobile.tta.interfaces.OnResponseCallback;
import org.tta.mobile.tta.ui.base.BasePagerAdapter;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.tta.mobile.tta.ui.interfaces.SearchPageOpenedListener;
import org.tta.mobile.tta.ui.library.LibraryTab;
import org.tta.mobile.view.common.PageViewStateCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LibraryViewModel extends BaseViewModel {

    private List<Fragment> fragments;
    private List<String> titles;
    public ListingPagerAdapter adapter;

    private CollectionConfigResponse cr;
    private List<Category> categories;
    private SearchPageOpenedListener searchPageOpenedListener;

    public ObservableInt initialPosition = new ObservableInt();
    public ObservableInt toolTipPosition = new ObservableInt();
    public ObservableInt toolTipGravity = new ObservableInt();
    public ObservableField<String> toolTiptext = new ObservableField<>();


    public ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            initialPosition.set(i);
            PageViewStateCallback callback = (PageViewStateCallback) fragments.get(i);
            if (callback != null) {
                callback.onPageShow();
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    public LibraryViewModel(Context context, TaBaseFragment fragment, SearchPageOpenedListener searchPageOpenedListener) {
        super(context, fragment);

        categories = new ArrayList<>();
        fragments = new ArrayList<>();
        titles = new ArrayList<>();
        this.searchPageOpenedListener = searchPageOpenedListener;

        adapter = new ListingPagerAdapter(mFragment.getChildFragmentManager());

        getData();
//        setToolTip();

    }

    private void getData() {
        mActivity.showLoading();

        mDataManager.getCollectionConfig(new OnResponseCallback<CollectionConfigResponse>() {
            @Override
            public void onSuccess(CollectionConfigResponse data) {
//                mActivity.hideLoading();
                cr = data;

                if (cr != null) {
                    categories.clear();
                    categories.addAll(cr.getCategory());
                    Collections.sort(categories);
                }

                populateTabs();
//                setToolTip();

            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
//                mActivity.showShortSnack(e.getLocalizedMessage());
            }
        });

    }

    private void populateTabs() {
        fragments.clear();
        titles.clear();
        for (Category category : categories) {
            fragments.add(LibraryTab.newInstance(cr, category, searchPageOpenedListener));
            titles.add(category.getName());
            Log.d(">>>>>...Tabs: ", category.getName());
        }

        try {
            adapter.setFragments(fragments, titles);
        } catch (Exception e) {
            Bundle parameters = new Bundle();
            parameters.putString(Constants.KEY_CLASS_NAME, LibraryViewModel.class.getName());
            parameters.putString(Constants.KEY_FUNCTION_NAME, "populateTabs");
            Logger.logCrashlytics(e, parameters);
            e.printStackTrace();
        }
        initialPosition.set(0);

        if (!categories.isEmpty()) {
            PageViewStateCallback callback = (PageViewStateCallback) fragments.get(0);
            if (callback != null) {
                callback.onPageShow();
            }
        }

//        toolTipPosition.set(0);

    }

    public class ListingPagerAdapter extends BasePagerAdapter {
        public ListingPagerAdapter(FragmentManager fm) {
            super(fm);
        }
    }

    public void setToolTip() {
        if (!mDataManager.getAppPref().isProfileVisited()) {
            toolTipGravity.set(Gravity.BOTTOM);
            toolTiptext.set("प्रत्येक बटन पर क्लिक करके \nविशिष्ट सामग्री पाएँ ");
            toolTipPosition.set(initialPosition.get());
//            mDataManager.getAppPref().setProfileVisited(true);

        }
    }
}
