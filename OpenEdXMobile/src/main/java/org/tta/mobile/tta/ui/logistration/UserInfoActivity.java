package org.tta.mobile.tta.ui.logistration;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import org.tta.mobile.R;
import org.tta.mobile.module.registration.model.RegistrationOption;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.data.model.authentication.FieldInfo;
import org.tta.mobile.tta.data.model.authentication.Profession;
import org.tta.mobile.tta.data.model.authentication.StateCustomAttribute;
import org.tta.mobile.tta.data.model.search.FilterSection;
import org.tta.mobile.tta.data.model.search.FilterTag;
import org.tta.mobile.tta.data.model.search.SearchFilter;
import org.tta.mobile.tta.interfaces.OnResponseCallback;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.custom.FormEditText;
import org.tta.mobile.tta.ui.custom.FormMultiSpinner;
import org.tta.mobile.tta.ui.custom.FormSpinner;
import org.tta.mobile.tta.ui.logistration.view_model.UserInfoViewModel;
import org.tta.mobile.tta.utils.DataUtil;
import org.tta.mobile.tta.utils.ViewUtil;

import java.util.ArrayList;
import java.util.List;

public class UserInfoActivity extends BaseVMActivity {
    private LinearLayout userInfoLayout;
    private FormEditText etFirstName;
    private FormSpinner stateSpinner;
    private FormSpinner districtSpinner;
    private FormSpinner blockSpinner;
    private FormSpinner professionSpinner;
    private FormSpinner genderSpinner;
    private FormMultiSpinner classTaughtSpinner;
    private FormMultiSpinner skillsSpinner;
    private FormMultiSpinner subjectsSpinner;
    private FormSpinner dietSpinner;
    private FormEditText etPmis;
    private Button btn;
    private TextView privacyLinkText;
    private Toolbar toolbar;
    private UserInfoViewModel mViewModel;

    private FieldInfo fieldInfo;
    private String pmisError;
    private String delimiterTagChunks, delimiterSectionTag, replacementTagSpace;
    public static boolean isAlreadyOpened;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new UserInfoViewModel(this);
        delimiterTagChunks = Constants.DELIMITER_TAG_CHUNKS;
        delimiterSectionTag = Constants.DELIMITER_SECTION_TAG;
        replacementTagSpace = Constants.REPLACEMENT_TAG_SPACE;
        binding(R.layout.t_activity_user_info, mViewModel);

        userInfoLayout = findViewById(R.id.user_info_fields_layout);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getCustomFieldAttributes();
        mViewModel.getData();
        setupForm();
        getBlocks();
        getClassSkillSubject();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getCustomFieldAttributes(){

        mViewModel.getDataManager().getCustomFieldAttributes(new OnResponseCallback<FieldInfo>() {
            @Override
            public void onSuccess(FieldInfo data) {
                fieldInfo = data;
            }

            @Override
            public void onFailure(Exception e) {

            }
        });

    }

    private void getBlocks() {
        if (mViewModel.currentState == null || mViewModel.currentDistrict == null) {
            mViewModel.blocks.clear();
            blockSpinner.setItems(mViewModel.blocks, null);
            return;
        }
        showLoading();
        mViewModel.getBlocks(
                new OnResponseCallback<List<RegistrationOption>>() {
                    @Override
                    public void onSuccess(List<RegistrationOption> data) {
                        hideLoading();
                        blockSpinner.setItems(mViewModel.blocks, null);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        hideLoading();
                    }
                });
    }

    private void getClassSkillSubject() {

        mViewModel.getClassSkillSubject(new OnResponseCallback<List<RegistrationOption>>() {
            @Override
            public void onSuccess(List<RegistrationOption> data) {
                classTaughtSpinner.setItems(data, null);
            }

            @Override
            public void onFailure(Exception e) {

            }
        }, new OnResponseCallback<List<RegistrationOption>>() {
            @Override
            public void onSuccess(List<RegistrationOption> data) {
                skillsSpinner.setItems(data, null);
            }

            @Override
            public void onFailure(Exception e) {

            }
        }, new OnResponseCallback<List<RegistrationOption>>() {
            @Override
            public void onSuccess(List<RegistrationOption> data) {
                subjectsSpinner.setItems(data, null);
            }

            @Override
            public void onFailure(Exception e) {

            }
        });

    }

    private void setupForm() {

        ViewUtil.addEmptySpace(userInfoLayout, (int) getResources().getDimension(R.dimen._14dp));

        etFirstName = ViewUtil.addFormEditText(userInfoLayout, "Name/नाम");
        etFirstName.setSingleLine();
        etFirstName.setMandatory(true);

        stateSpinner = ViewUtil.addOptionSpinner(userInfoLayout, "State/राज्य*", mViewModel.states, null);
        stateSpinner.setMandatory(true);

        districtSpinner = ViewUtil.addOptionSpinner(userInfoLayout, "District/जिला*", mViewModel.districts, null);
        districtSpinner.setMandatory(true);

        blockSpinner = ViewUtil.addOptionSpinner(userInfoLayout, "Block/खंड*", mViewModel.blocks, null);
        blockSpinner.setMandatory(true);

        professionSpinner = ViewUtil.addOptionSpinner(userInfoLayout, "Profession/व्यवसाय*", mViewModel.professions, null);
        professionSpinner.setMandatory(true);

        genderSpinner = ViewUtil.addOptionSpinner(userInfoLayout, "Gender/लिंग*", mViewModel.genders, null);
        genderSpinner.setMandatory(true);

        classTaughtSpinner = ViewUtil.addMultiOptionSpinner(userInfoLayout, "Classes Taught/पढ़ाई गई कक्षा*",
                mViewModel.classesTaught, null);
        classTaughtSpinner.setMandatory(true);

        skillsSpinner = ViewUtil.addMultiOptionSpinner(userInfoLayout, "दी गयी क्षमताओं में से आप किन में खुद को निपुण मानते हैं ? कोई 5 चुनिए |*",
                mViewModel.skills, null);
        skillsSpinner.setSelectionLimit(5);
        skillsSpinner.setMandatory(true);

        subjectsSpinner = ViewUtil.addMultiOptionSpinner(userInfoLayout, "आपको किन विषयों में दिलचस्पी हैं ?*",
                mViewModel.subjects, null);
        subjectsSpinner.setMandatory(true);

        etPmis = ViewUtil.addFormEditText(userInfoLayout, "PMIS Code/पी इम आइ इस कोड");
        etPmis.setSingleLine();
        setCustomField(mViewModel.currentState, mViewModel.currentProfession);

        dietSpinner = ViewUtil.addOptionSpinner(userInfoLayout, "DIET Code/डी आइ इ टी कोड", mViewModel.dietCodes, null);
        toggleDietCodeVisibility();

        btn = ViewUtil.addButton(userInfoLayout, getString(R.string.submit));
        privacyLinkText = ViewUtil.addLinkText(userInfoLayout, "Privacy Policy");
        privacyLinkText.setVisibility(View.GONE);
        ViewUtil.addEmptySpace(userInfoLayout, (int) getResources().getDimension(R.dimen._50px));

        setListeners();
    }

    private void setListeners() {
        btn.setOnClickListener(v -> {
            if (!validate()) {
                return;
            }
            Bundle parameters = new Bundle();
            parameters.putString("name", etFirstName.getText().trim());
            parameters.putString("state", stateSpinner.getSelectedOption().getValue());
            parameters.putString("district", mViewModel.currentDistrict);
            parameters.putString("block", blockSpinner.getSelectedOption().getName());
            parameters.putString("title", professionSpinner.getSelectedOption().getValue());
            parameters.putString("gender", genderSpinner.getSelectedOption().getValue());

            List<FilterSection> sections = new ArrayList<>();
            StringBuilder builder = new StringBuilder();

            if (classTaughtSpinner.getSelectedOptions() != null && mViewModel.classSection != null) {
                List<FilterTag> tags = new ArrayList<>();
                List<FilterTag> classTags = mViewModel.classSection.getTags();

                for (RegistrationOption option: classTaughtSpinner.getSelectedOptions()){

                    FilterTag tag = new FilterTag();
                    tag.setValue(option.getName());
                    if (classTags.contains(tag)){
                        tags.add(classTags.get(classTags.indexOf(tag)));
                    }

                    builder.append(mViewModel.classesSectionName)
                            .append(delimiterSectionTag)
                            .append(option.getName())
                            .append(delimiterTagChunks);
                }

                if (!tags.isEmpty()){
                    FilterSection section = new FilterSection();
                    section.setTags(tags);
                    section.setName(mViewModel.classSection.getName());
                    section.setId(mViewModel.classSection.getId());
                    section.setIn_profile(mViewModel.classSection.isIn_profile());
                    section.setOrder(mViewModel.classSection.getOrder());
                    sections.add(section);
                }
            }

            if (skillsSpinner.getSelectedOptions() != null && mViewModel.skillSection != null) {
                List<FilterTag> tags = new ArrayList<>();
                List<FilterTag> skillTags = mViewModel.skillSection.getTags();

                for (RegistrationOption option: skillsSpinner.getSelectedOptions()){

                    FilterTag tag = new FilterTag();
                    tag.setValue(option.getName());
                    if (skillTags.contains(tag)){
                        tags.add(skillTags.get(skillTags.indexOf(tag)));
                    }

                    builder.append(mViewModel.skillSectionName)
                            .append(delimiterSectionTag)
                            .append(option.getName())
                            .append(delimiterTagChunks);
                }

                if (!tags.isEmpty()){
                    FilterSection section = new FilterSection();
                    section.setTags(tags);
                    section.setName(mViewModel.skillSection.getName());
                    section.setId(mViewModel.skillSection.getId());
                    section.setIn_profile(mViewModel.skillSection.isIn_profile());
                    section.setOrder(mViewModel.skillSection.getOrder());
                    sections.add(section);
                }
            }

            if (subjectsSpinner.getSelectedOptions() != null && mViewModel.subjectSection != null) {
                List<FilterTag> tags = new ArrayList<>();
                List<FilterTag> subjectTags = mViewModel.subjectSection.getTags();

                for (RegistrationOption option: subjectsSpinner.getSelectedOptions()){

                    FilterTag tag = new FilterTag();
                    tag.setValue(option.getName());
                    if (subjectTags.contains(tag)){
                        tags.add(subjectTags.get(subjectTags.indexOf(tag)));
                    }

                    builder.append(mViewModel.subjectSectionName)
                            .append(delimiterSectionTag)
                            .append(option.getName())
                            .append(delimiterTagChunks);
                }

                if (!tags.isEmpty()){
                    FilterSection section = new FilterSection();
                    section.setTags(tags);
                    section.setName(mViewModel.subjectSection.getName());
                    section.setId(mViewModel.subjectSection.getId());
                    section.setIn_profile(mViewModel.subjectSection.isIn_profile());
                    section.setOrder(mViewModel.subjectSection.getOrder());
                    sections.add(section);
                }
            }

            String label = "";
            if (builder.length() > 0){
                label = builder.substring(0, builder.length() - delimiterTagChunks.length());
            }
//            parameters.putString("tag_label", label);

            SearchFilter filter = new SearchFilter();
            filter.setResult(sections);
            parameters.putSerializable("tag_label", filter);

            if (etPmis.isVisible()) {
                parameters.putString("pmis_code", etPmis.getText());
            } else {
                parameters.putString("pmis_code", "");
            }

            if (dietSpinner.isVisible()) {
                parameters.putString("diet_code", dietSpinner.getSelectedOption().getName());
            } else {
                parameters.putString("diet_code", "");
            }

            mViewModel.submit(parameters);
        });

        privacyLinkText.setOnClickListener(v -> {
            mViewModel.getDataManager().getEdxEnvironment().getRouter().showAuthenticatedWebviewActivity(
                    this, getString(R.string.privacy_policy_url), "Privacy Policy"
            );
        });

        stateSpinner.setOnItemSelectedListener((view, item) -> {
            if (item == null){
                mViewModel.currentState = null;
                setCustomField(mViewModel.currentState, mViewModel.currentProfession);
                mViewModel.districts.clear();
                districtSpinner.setItems(mViewModel.districts, null);
                setProfessionItems();
                mViewModel.dietCodes.clear();
                dietSpinner.setItems(mViewModel.dietCodes, null);
                toggleDietCodeVisibility();
                return;
            }

            mViewModel.currentState = item.getName();
            setCustomField(mViewModel.currentState, mViewModel.currentProfession);

            mViewModel.districts.clear();
            mViewModel.districts = DataUtil.getDistrictsByStateName(mViewModel.currentState);
            districtSpinner.setItems(mViewModel.districts, null);

            setProfessionItems();
            mViewModel.dietCodes.clear();
            mViewModel.dietCodes = DataUtil.getAllDietCodesOfState(mViewModel.currentState);
            dietSpinner.setItems(mViewModel.dietCodes, null);
            toggleDietCodeVisibility();
        });

        districtSpinner.setOnItemSelectedListener((view, item) -> {
            if (item != null) {
                mViewModel.currentDistrict = item.getName();
            } else {
                mViewModel.currentDistrict = null;
            }
            getBlocks();
        });

        professionSpinner.setOnItemSelectedListener((view, item) -> {
            if (item != null) {
                mViewModel.currentProfession = item.getName();
            } else {
                mViewModel.currentProfession = null;
            }
            setCustomField(mViewModel.currentState, mViewModel.currentProfession);
            toggleDietCodeVisibility();
        });
    }

    private void setCustomField(String stateName, String professionName){
        if (etPmis == null){
            return;
        }
        if (stateName == null || stateName.equals("") ||
                professionName == null || professionName.equals("") ||
                fieldInfo == null){
            etPmis.setVisibility(View.GONE);
            return;
        }

        for (StateCustomAttribute attribute: fieldInfo.getStateCustomAttribute()){
            if (stateName.equalsIgnoreCase(attribute.getState())){
                for (Profession profession: attribute.getProfession()){
                    if (professionName.equalsIgnoreCase(profession.getValue())){
                        etPmis.setHint(attribute.getLabel());
                        etPmis.setSubLabel(attribute.getHelptext());
                        etPmis.setVisibility(View.VISIBLE);
                        pmisError = attribute.getPlaceholder();
                        return;
                    }
                }
            }
        }

        etPmis.setVisibility(View.GONE);
    }

    private void setProfessionItems(){
        if (professionSpinner == null){
            return;
        }

        mViewModel.professions.clear();
        if (mViewModel.currentState == null || mViewModel.currentState.equals("") || fieldInfo == null){
            mViewModel.professions.addAll(DataUtil.getAllProfessions());
            professionSpinner.setItems(mViewModel.professions, null);
            return;
        }

        for (StateCustomAttribute attribute: fieldInfo.getStateCustomAttribute()){
            if (mViewModel.currentState.equalsIgnoreCase(attribute.getState())){
                for (Profession profession: attribute.getProfession()){
                    mViewModel.professions.add(new RegistrationOption(profession.getValue(), profession.getKey()));
                }
                professionSpinner.setItems(mViewModel.professions, null);
                return;
            }
        }

        mViewModel.professions.addAll(DataUtil.getAllProfessions());
        professionSpinner.setItems(mViewModel.professions, null);

    }

    private void toggleDietCodeVisibility(){
        if (dietSpinner == null){
            return;
        }
        if (mViewModel.currentState == null || mViewModel.currentState.equals("") ||
                mViewModel.currentProfession == null || mViewModel.currentProfession.equals("")){
            dietSpinner.setVisibility(View.GONE);
            return;
        }
        if (mViewModel.currentState.equals("Chhattisgarh") && mViewModel.currentProfession.equals("Teacher Trainee")){
            dietSpinner.setVisibility(View.VISIBLE);
        } else {
            dietSpinner.setVisibility(View.GONE);
        }
    }

    private boolean validate(){
        boolean valid = true;
        if (!etFirstName.validate() ||
                (mViewModel.getDataManager().getLoginPrefs().getUsername() != null &&
                        etFirstName.getText().trim().equals(mViewModel.getDataManager().getLoginPrefs().getUsername()))){
            valid = false;
            etFirstName.setError(getString(R.string.error_name));
        }
        if (!stateSpinner.validate()){
            valid = false;
            stateSpinner.setError(getString(R.string.error_state));
        }
        if (!districtSpinner.validate()){
            valid = false;
            districtSpinner.setError(getString(R.string.error_district));
        }
        if (!blockSpinner.validate()){
            valid = false;
            blockSpinner.setError(getString(R.string.error_block));
        }
        if (!professionSpinner.validate()){
            valid = false;
            professionSpinner.setError(getString(R.string.error_profession));
        }
        if (!genderSpinner.validate()){
            valid = false;
            genderSpinner.setError(getString(R.string.error_gender));
        }
        if (!classTaughtSpinner.validate()){
            valid = false;
            classTaughtSpinner.setError(getString(R.string.error_classes));
        }
        if (!skillsSpinner.validate()){
            valid = false;
            skillsSpinner.setError(getString(R.string.error_skills));
        }
        if (!subjectsSpinner.validate()){
            valid = false;
            subjectsSpinner.setError(getString(R.string.error_subjects));
        }
        if (etPmis.isVisible() && !etPmis.validate()){
            valid = false;
            etPmis.setError(pmisError);
        }
        if (dietSpinner.isVisible() && !dietSpinner.validate()){
            valid = false;
            dietSpinner.setError(getString(R.string.error_diet));
        }

        return valid;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAlreadyOpened = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isAlreadyOpened = false;
    }
}
