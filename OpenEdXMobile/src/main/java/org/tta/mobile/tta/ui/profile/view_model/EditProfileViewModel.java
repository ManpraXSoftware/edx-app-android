package org.tta.mobile.tta.ui.profile.view_model;

import android.Manifest;
import android.content.Context;
import android.databinding.ObservableBoolean;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;


import org.tta.mobile.model.api.ProfileModel;
import org.tta.mobile.module.registration.model.RegistrationOption;
import org.tta.mobile.tta.data.model.profile.UpdateMyProfileResponse;
import org.tta.mobile.tta.data.model.search.FilterSection;
import org.tta.mobile.tta.data.model.search.FilterTag;
import org.tta.mobile.tta.data.model.search.SearchFilter;
import org.tta.mobile.tta.exception.TaException;
import org.tta.mobile.tta.interfaces.OnResponseCallback;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.tta.mobile.tta.utils.DataUtil;
import org.tta.mobile.user.Account;
import org.tta.mobile.user.ProfileImage;
import org.tta.mobile.util.PermissionsUtil;

import java.util.ArrayList;
import java.util.List;

public class EditProfileViewModel extends BaseViewModel  {

    public ProfileModel profileModel;
    public ProfileImage profileImage;
    private Account account;
    private SearchFilter searchFilter;
    private String tagLabel;
    private Uri imageUri;
    private Rect cropRect;

    public ObservableBoolean imageAddVisible = new ObservableBoolean();

    private boolean imageChanged;
    private boolean profileReceived, imageReceived;
    private boolean profileSuccessful, imageSuccessful;

    public List<RegistrationOption> states;
    public List<RegistrationOption> districts;
    public List<RegistrationOption> blocks;
    public List<RegistrationOption> professions;
    public List<RegistrationOption> genders;
    public List<RegistrationOption> classesTaught;
    public List<RegistrationOption> skills;
    public List<RegistrationOption> dietCodes;
    public List<RegistrationOption> organisation;

    public String currentState, currentDistrict, currentProfession;
    public String classesSectionName, skillSectionName;

    public EditProfileViewModel(Context context, TaBaseFragment fragment,
                                ProfileModel profileModel, ProfileImage profileImage,
                                Account account, SearchFilter searchFilter) {
        super(context, fragment);

        this.profileModel = profileModel;
        this.profileImage = profileImage;
        this.account = account;
        this.searchFilter = searchFilter;

        states = new ArrayList<>();
        districts = new ArrayList<>();
        blocks = new ArrayList<>();
        professions = new ArrayList<>();
        genders = new ArrayList<>();
        classesTaught = new ArrayList<>();
        skills = new ArrayList<>();
        dietCodes = new ArrayList<>();
        organisation = new ArrayList<>();

        if (profileImage == null || profileImage.getImageUrlLarge() == null){
            imageAddVisible.set(true);
        } else {
            imageAddVisible.set(false);
        }

    }

    public void takePhoto(){
        mFragment.askForPermissions(new String[]{Manifest.permission.CAMERA},
                PermissionsUtil.CAMERA_PERMISSION_REQUEST);
    }

    public void choosePhoto(){
        mFragment.askForPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PermissionsUtil.READ_STORAGE_PERMISSION_REQUEST);
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
        imageChanged = true;
    }

    public void setCropRect(Rect cropRect) {
        this.cropRect = cropRect;
        imageChanged = true;
    }

    public void submit(Bundle parameters){
        mActivity.showLoading();
        profileReceived = false;
        profileSuccessful = false;
        imageReceived = false;
        imageSuccessful = false;

        if (imageUri != null && cropRect != null && imageChanged) {
            mDataManager.updateProfileImage(imageUri, cropRect, new OnResponseCallback<ProfileImage>() {
                @Override
                public void onSuccess(ProfileImage data) {
                    imageChanged = false;
                    imageReceived = true;
                    imageSuccessful = true;
                    hideLoading();
                    goBack();
                }

                @Override
                public void onFailure(Exception e) {
                    imageReceived = true;
                    hideLoading();
                    mActivity.showLongSnack(e.getLocalizedMessage());
                }
            });
        } else {
            imageReceived = true;
            imageSuccessful = true;
        }

        mDataManager.updateProfile(parameters, new OnResponseCallback<UpdateMyProfileResponse>() {
            @Override
            public void onSuccess(UpdateMyProfileResponse data) {
                profileReceived = true;
                profileSuccessful = true;
                hideLoading();
                goBack();
            }

            @Override
            public void onFailure(Exception e) {
                profileReceived = true;
                hideLoading();
                mActivity.showLongSnack(e.getLocalizedMessage());
            }
        });

    }

    private void hideLoading(){
        if (imageReceived && profileReceived){
            mActivity.hideLoading();
        }
    }

    private void goBack(){
        if (profileSuccessful && imageSuccessful){
            mActivity.showLongSnack("Profile updated successfully");
            mActivity.onBackPressed();
        }
    }

    public void getBlocks(OnResponseCallback<List<RegistrationOption>> callback){
        Bundle parameters = new Bundle();

        parameters.putString("state",currentState);
        parameters.putString("district",currentDistrict);

        mDataManager.getBlocks(callback, parameters, blocks);
    }

    public void getClassesAndSkills(OnResponseCallback<List<RegistrationOption>> classesCallback,
                                    OnResponseCallback<List<RegistrationOption>> skillsCallback){

        if (searchFilter != null){

            for (FilterSection section : searchFilter.getResult()) {
                if (section.isIn_profile() && section.getTags() != null) {
                    if (section.getName().contains("कक्षा")) {
                        classesSectionName = section.getName();
                        for (FilterTag tag: section.getTags()){
                            classesTaught.add(new RegistrationOption(tag.toString(), tag.toString()));
                        }
                    } else if (section.getName().contains("कौशल")){
                        skillSectionName = section.getName();
                        for (FilterTag tag: section.getTags()){
                            skills.add(new RegistrationOption(tag.toString(), tag.toString()));
                        }
                    }
                }
            }
            classesCallback.onSuccess(classesTaught);
            skillsCallback.onSuccess(skills);

        } else {
            classesCallback.onFailure(new TaException("Classes not found"));
            skillsCallback.onFailure(new TaException("Skills not found"));
        }
    }

    public void getData() {
        states.addAll(DataUtil.getAllStates());
        currentState = states.get(0).getName();
        districts.addAll(DataUtil.getDistrictsByStateName(states.get(0).getName()));
        currentDistrict = districts.get(0).getName();
        professions.addAll(DataUtil.getAllProfessions());
        genders.addAll(DataUtil.getAllGenders());
        dietCodes.addAll(DataUtil.getAllDietCodesOfState(currentState));
        organisation.addAll(DataUtil.getAllOrganisation());
    }
}
