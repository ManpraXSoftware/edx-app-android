package org.tta.mobile.tta.ui.logistration.view_model;

import android.content.Intent;
import android.os.Bundle;

import org.tta.mobile.R;
import org.tta.mobile.logger.Logger;
import org.tta.mobile.model.api.ProfileModel;
import org.tta.mobile.module.registration.model.RegistrationOption;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.analytics.analytics_enums.Action;
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.analytics.analytics_enums.Source;
import org.tta.mobile.tta.data.model.profile.UpdateMyProfileResponse;
import org.tta.mobile.tta.data.model.search.FilterSection;
import org.tta.mobile.tta.data.model.search.FilterTag;
import org.tta.mobile.tta.data.model.search.SearchFilter;
import org.tta.mobile.tta.interfaces.OnResponseCallback;
import org.tta.mobile.tta.task.profile.UpdateMyProfileTask;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.tta.mobile.tta.ui.landing.LandingActivity;
import org.tta.mobile.tta.utils.ActivityUtil;
import org.tta.mobile.tta.utils.DataUtil;
import org.tta.mobile.util.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

public class UserInfoViewModel extends BaseViewModel {

    public List<RegistrationOption> states;
    public List<RegistrationOption> districts;
    public List<RegistrationOption> blocks;
    public List<RegistrationOption> professions;
    public List<RegistrationOption> genders;
    public List<RegistrationOption> classesTaught;
    public List<RegistrationOption> skills;
    public List<RegistrationOption> dietCodes;
    public List<RegistrationOption> subjects;

    public String currentState, currentDistrict, currentProfession;
    public String classesSectionName, skillSectionName, subjectSectionName;
    public FilterSection classSection, skillSection, subjectSection;

    public UserInfoViewModel(BaseVMActivity activity) {
        super(activity);

        states = new ArrayList<>();
        districts = new ArrayList<>();
        blocks = new ArrayList<>();
        professions = new ArrayList<>();
        genders = new ArrayList<>();
        classesTaught = new ArrayList<>();
        skills = new ArrayList<>();
        dietCodes = new ArrayList<>();
        subjects = new ArrayList<>();
    }

    public void logout(){
        mDataManager.logout();
    }

    public void getBlocks(OnResponseCallback<List<RegistrationOption>> callback){
        Bundle parameters = new Bundle();

        parameters.putString("state",currentState);
        parameters.putString("district",currentDistrict);

        mDataManager.getBlocks(callback, parameters, blocks);
    }

    public void getClassSkillSubject(OnResponseCallback<List<RegistrationOption>> classesCallback,
                                     OnResponseCallback<List<RegistrationOption>> skillsCallback,
                                     OnResponseCallback<List<RegistrationOption>> subjectsCallback){

        mDataManager.getSearchFilter(new OnResponseCallback<SearchFilter>() {
            @Override
            public void onSuccess(SearchFilter data) {
                for (FilterSection section : data.getResult()) {
                    if (section.isIn_profile() && section.getTags() != null) {
                        if (section.getName().contains("कक्षा")) {
                            classesSectionName = section.getName();
                            classSection = section;
                            for (FilterTag tag: section.getTags()){
                                if (tag.isIn_profile()) {
                                    classesTaught.add(new RegistrationOption(tag.toString(), tag.toString()));
                                }
                            }
                        } else if (section.getName().contains("क्षमता")){
                            skillSectionName = section.getName();
                            skillSection = section;
                            for (FilterTag tag: section.getTags()){
                                if (tag.isIn_profile()) {
                                    skills.add(new RegistrationOption(tag.toString(), tag.toString()));
                                }
                            }
                        } else if (section.getName().contains("विषय")){
                            subjectSectionName = section.getName();
                            subjectSection = section;
                            for (FilterTag tag: section.getTags()){
                                if (tag.isIn_profile()) {
                                    subjects.add(new RegistrationOption(tag.toString(), tag.toString()));
                                }
                            }
                        }
                    }
                }
                classesCallback.onSuccess(classesTaught);
                skillsCallback.onSuccess(skills);
                subjectsCallback.onSuccess(subjects);
            }

            @Override
            public void onFailure(Exception e) {
                classesCallback.onFailure(e);
                skillsCallback.onFailure(e);
                subjectsCallback.onFailure(e);
            }
        });
    }

    public void submit(Bundle parameters){
        if (!NetworkUtil.isConnected(mActivity)){
            mActivity.showLongSnack(mActivity.getString(R.string.no_connection_exception));
            return;
        }
        mActivity.showLoading();

        new UpdateMyProfileTask(mActivity, parameters,mDataManager.getLoginPrefs().getUsername())
        {
            @Override
            protected void onSuccess(UpdateMyProfileResponse response) throws Exception {
                super.onSuccess(response);
                mActivity.hideLoading();

                //for cache consistency
                ProfileModel profileModel = new ProfileModel();
                profileModel.name = response.getName();
                profileModel.email = response.getEmail();
                profileModel.gender=response.getGender();

                profileModel.title=response.getTitle();
                profileModel.classes_taught=response.getClasses_taught();
                profileModel.state=response.getState();
                profileModel.district=response.getDistrict();
                profileModel.block=response.getBlock();
                profileModel.pmis_code=response.getPMIS_code();
                profileModel.diet_code=response.getDIETCode();
                profileModel.setTagLabel(response.getTagLabel());

                //for analytics update
                /*Analytic aHelper=new Analytic();
                aHelper.updateAnalytics(getApplicationContext(), aHelper.getAnalyticParams(loginPrefs.getUsername(),
                        "RegisterComplete", Action.Registration,
                        String.valueOf(Page.RegistrationPage), Source.Mobile));*/

                mDataManager.getLoginPrefs().setCurrentUserProfileInCache(profileModel);

                if(response.getRegistrationError()!=null &&
                        response.getRegistrationError().getField_name()!=null)
                {
                    mActivity.showErrorDialog(response.getRegistrationError().getTitle(),response.getRegistrationError().getMessage());
                }
                else
                {
                    mActivity.showLongSnack(mActivity.getString(R.string.registered_successfully));
                    performBackgroundTasks();
                    ActivityUtil.gotoPage(mActivity, LandingActivity.class, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mActivity.finish();

                    mActivity.analytic.addMxAnalytics_db(null, Action.RegisterComplete, Nav.register.name(),
                            Source.Mobile, null);
                }
            }

            @Override
            protected void onException(Exception ex) {
                Bundle parameters1 = new Bundle();
                parameters1.putString(Constants.KEY_CLASS_NAME, UserInfoViewModel.class.getName());
                parameters1.putString(Constants.KEY_FUNCTION_NAME, "submit");
                parameters1.putString(Constants.KEY_DATA, "parameters = " + parameters);
                Logger.logCrashlytics(ex, parameters1);
                mActivity.hideLoading();
                mActivity.showErrorDialog(null,mActivity.getString(R.string.action_not_completed));
            }
        }.execute();

    }

    private void performBackgroundTasks(){
        mDataManager.refreshLocalDatabase();
        mDataManager.scheduleDeleteFeeds();
        mDataManager.updateFirebaseToken(getActivity());
        mDataManager.scheduleSyncAnalyticsJob();
    }

    public void getData() {
        states.addAll(DataUtil.getAllStates());
        districts.addAll(DataUtil.getDistrictsByStateName(states.get(0).getName()));
        professions.addAll(DataUtil.getAllProfessions());
        genders.addAll(DataUtil.getAllGenders());
        dietCodes.addAll(DataUtil.getAllDietCodesOfState(currentState));
    }
}
