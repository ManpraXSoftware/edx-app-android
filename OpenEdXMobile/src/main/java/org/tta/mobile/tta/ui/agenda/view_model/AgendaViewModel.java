package org.tta.mobile.tta.ui.agenda.view_model;

import android.content.Context;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.databinding.ViewDataBinding;

import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.tta.mobile.R;
import org.tta.mobile.databinding.TRowAgendaItemBinding;
import org.tta.mobile.tta.data.local.db.table.Source;
import org.tta.mobile.tta.data.model.agenda.AgendaItem;
import org.tta.mobile.tta.data.model.agenda.AgendaList;
import org.tta.mobile.tta.interfaces.OnResponseCallback;
import org.tta.mobile.tta.ui.agenda_items.AgendaListFragment;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.tta.mobile.tta.utils.ActivityUtil;
import org.tta.mobile.tta.utils.ContentSourceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AgendaViewModel extends BaseViewModel {

    public List<Source> sources;

    public ObservableField<String> regionListTitle = new ObservableField<>("Region");
    public ObservableField<String> regionToolTip;
    public ObservableField<String> personalToolTip;
    public ObservableField<String> downloadToolTip;
    public ObservableInt toolTipGravity = new ObservableInt(Gravity.BOTTOM);
    public ObservableInt personalToolTipGravity = new ObservableInt(Gravity.TOP);

    public AgendaListAdapter stateListAdapter, myListAdapter, downloadListAdapter;

    private boolean regionListReceived, myListReceived, downloadListReceived;

    public AgendaViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);
        stateListAdapter = new AgendaListAdapter(mActivity, mActivity.getString(R.string.state_wise_list));
        myListAdapter = new AgendaListAdapter(mActivity, mActivity.getString(R.string.my_agenda));
        downloadListAdapter = new AgendaListAdapter(mActivity, mActivity.getString(R.string.download));
//        setToolTip();

    }

    public void setToolTip(){
        if (!mDataManager.getAppPref().isAgendaVisited()){
            regionToolTip = new ObservableField<>(mActivity.getResources().getString(R.string.region_btn_agenda));
            personalToolTip = new ObservableField<>(mActivity.getResources().getString(R.string.personal_btn_agenda));
            downloadToolTip = new ObservableField<>(mActivity.getResources().getString(R.string.download_btn_agenda));
            mDataManager.getAppPref().setAgendaVisited(true);
        }else {
            if (regionToolTip!=null)
            regionToolTip.set("");
            if (personalToolTip!=null)
            personalToolTip.set("");
            if (downloadToolTip!=null)
            downloadToolTip.set("");
        }

    }

    public void getAgenda() {
        mActivity.showLoading();

        mDataManager.getSources(new OnResponseCallback<List<Source>>() {
            @Override
            public void onSuccess(List<Source> data) {
                sources = data;
                getRegionAgenda();
                getMyAgenda();
                getDownloadAgenda();
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
//                mActivity.showLongSnack(e.getLocalizedMessage());
            }
        });

    }

    private void getRegionAgenda() {
        regionListReceived = false;
        mDataManager.getStateAgendaCount(new OnResponseCallback<List<AgendaList>>() {
            @Override
            public void onSuccess(List<AgendaList> data) {
                regionListReceived = true;
                hideLoader();
                if (data != null && !data.isEmpty()) {

                    AgendaList list = data.get(0);
                    stateListAdapter.setAgendaList(list);
                    if (list == null || list.getResult() == null || list.getResult().isEmpty()) {
                        showEmptyAgendaList(stateListAdapter);
                    } else {
                        List<AgendaItem> items = list.getResult();
//                        if (items.size() != sources.size()) {

                            for (Source source : sources) {
                                AgendaItem item = new AgendaItem();
                                item.setSource_name(source.getName());
                                if (!items.contains(item)) {
                                    item.setSource_title(source.getTitle());
                                    item.setContent_count(0);
                                    item.setOrder(source.getOrder());
                                    items.add(item);
                                } else {
                                    items.get(items.indexOf(item)).setOrder(source.getOrder());
                                }
                            }

//                        }
                        sortAgendaItems(items);
                        stateListAdapter.setItems(list.getResult());
                    }
                } else {
                    showEmptyAgendaList(stateListAdapter);
                }
            }

            @Override
            public void onFailure(Exception e) {
                regionListReceived = true;
                hideLoader();
                showEmptyAgendaList(stateListAdapter);
            }
        });

    }

    private void showEmptyAgendaList(AgendaListAdapter adapter) {
        List<AgendaItem> items = new ArrayList<>();

        for (Source source : sources) {
            AgendaItem item = new AgendaItem();
            item.setSource_title(source.getTitle());
            item.setSource_name(source.getName());
            item.setContent_count(0);
            item.setOrder(source.getOrder());
            items.add(item);
        }

        sortAgendaItems(items);
        adapter.setItems(items);
    }

    private void sortAgendaItems(List<AgendaItem> items){
        Collections.sort(items);
    }

    private void getMyAgenda() {
        myListReceived = false;
        mDataManager.getMyAgendaCount(new OnResponseCallback<AgendaList>() {
            @Override
            public void onSuccess(AgendaList data) {
                myListReceived = true;
                hideLoader();
                if (data != null && data.getResult() != null && !data.getResult().isEmpty()) {
                    List<AgendaItem> items = data.getResult();
//                    if (items.size() != sources.size()) {

                        for (Source source : sources) {
                            AgendaItem item = new AgendaItem();
                            item.setSource_name(source.getName());
                            item.setOrder(source.getOrder());
                            if (!items.contains(item)) {
                                item.setSource_title(source.getTitle());
                                item.setContent_count(0);
                                item.setOrder(source.getOrder());
                                items.add(item);
                            } else {
                                items.get(items.indexOf(item)).setOrder(source.getOrder());
                            }
                        }

//                    }
                    sortAgendaItems(items);
                    myListAdapter.setItems(data.getResult());
                } else {
                    showEmptyAgendaList(myListAdapter);
                }
            }

            @Override
            public void onFailure(Exception e) {
                myListReceived = true;
                hideLoader();
                showEmptyAgendaList(myListAdapter);
            }
        });
    }

    private void getDownloadAgenda() {
        mActivity.showLoading();
        downloadListReceived = false;

        mDataManager.getDownloadAgendaCount(sources, new OnResponseCallback<AgendaList>() {
            @Override
            public void onSuccess(AgendaList data) {
                downloadListReceived = true;
                hideLoader();
                if (data != null && data.getResult() != null && !data.getResult().isEmpty()) {
                    List<AgendaItem> items = data.getResult();
                    if (items.size() != sources.size()) {

                        for (Source source : sources) {
                            AgendaItem item = new AgendaItem();
                            item.setSource_name(source.getName());
                            if (!items.contains(item)) {
                                item.setSource_title(source.getTitle());
                                item.setContent_count(0);
                                item.setOrder(source.getOrder());
                                items.add(item);
                            }
                        }

                    }
                    sortAgendaItems(items);
                    downloadListAdapter.setItems(data.getResult());
                } else {
                    showEmptyAgendaList(downloadListAdapter);
                }
            }

            @Override
            public void onFailure(Exception e) {
                downloadListReceived = true;
                hideLoader();
                showEmptyAgendaList(downloadListAdapter);
            }
        });
    }

    private void hideLoader() {
        if (regionListReceived && myListReceived && downloadListReceived) {
            mActivity.hideLoading();
        }
    }


    public class AgendaListAdapter extends MxFiniteAdapter<AgendaItem> {
        private String agendaListName;
        private AgendaList agendaList;

        AgendaListAdapter(Context context, String string) {
            super(context);
            agendaListName = string;
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull AgendaItem model, @Nullable OnRecyclerItemClickListener<AgendaItem> listener) {
            if (binding instanceof TRowAgendaItemBinding) {
                TRowAgendaItemBinding itemBinding = (TRowAgendaItemBinding) binding;
                if (model.getContent_count() > 0) {
                    itemBinding.agendaCard.setCardBackgroundColor(ContextCompat.getColor(mActivity, ContentSourceUtil.getSourceColor(model.getSource_name())));
                } else {
                    itemBinding.agendaCard.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.secondary_grey_light));
                }
//                if (getItemPosition(model)==0){
//                    ToolTipView.showToolTip(mActivity,"आपके द्वारा डाउनलोड की गयी सामग्री यहाँ है", itemBinding.agendaCard, Gravity.TOP);
//                }

                itemBinding.agendaItemCount.setText(String.valueOf(model.getContent_count()));
                itemBinding.agendaSource.setText(model.getSource_title());
                itemBinding.agendaSource.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        ContentSourceUtil.getSourceDrawable_15x15(model.getSource_name()),
                        0, 0, 0
                );
                itemBinding.agendaCard.setOnClickListener(v -> ActivityUtil.replaceFragmentInActivity(
                        mActivity.getSupportFragmentManager(),
                        AgendaListFragment.newInstance(agendaListName, getItems(), model, agendaList),
                        R.id.dashboard_fragment,
                        AgendaListFragment.TAG,
                        true,
                        null
                ));
            }
        }

        void setAgendaList(AgendaList agendaList) {
            this.agendaList = agendaList;
        }
    }
}
