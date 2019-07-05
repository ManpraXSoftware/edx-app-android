package org.tta.mobile.tta.ui.agenda;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.tta.mobile.R;
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.ui.agenda.view_model.AgendaViewModel;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.utils.BreadcrumbUtil;

public class AgendaFragment extends TaBaseFragment {
    public static final String TAG = AgendaFragment.class.getCanonicalName();
    private static final int RANK = 2;

    private AgendaViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new AgendaViewModel(getActivity(), this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_agenda, viewModel)
                .getRoot();
        viewModel.getAgenda();
        viewModel.setToolTip();
//       if ( mBinding instanceof TFragmentAgendaBinding){
//           TFragmentAgendaBinding binding= (TFragmentAgendaBinding) mBinding;
//           ToolTipView.showToolTip(getActivity(), "fbdskj",binding.stateAgendaList.getTitleTextView(),
//                   Gravity.BOTTOM);
//
//       }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.agenda.name()));
    }
}
