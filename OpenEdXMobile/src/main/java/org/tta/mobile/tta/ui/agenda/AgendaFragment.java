package org.tta.mobile.tta.ui.agenda;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.maurya.mx.mxlib.view.MxFiniteRecyclerView;

import org.tta.mobile.R;
import org.tta.mobile.databinding.TFragmentAgendaBinding;
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.ui.agenda.view_model.AgendaViewModel;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.utils.BreadcrumbUtil;
import org.tta.mobile.tta.utils.ToolTipView;

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
