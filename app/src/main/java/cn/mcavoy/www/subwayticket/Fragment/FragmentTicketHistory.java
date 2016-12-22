package cn.mcavoy.www.subwayticket.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import cn.mcavoy.www.subwayticket.R;

public class FragmentTicketHistory extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ticket_history_main, container, false);
        setHasOptionsMenu(true);

        ViewGroup tab = (ViewGroup) view.findViewById(R.id.tab);
        tab.addView(inflater.from(view.getContext()).inflate(R.layout.distribute_evenly, tab, false));

        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        SmartTabLayout viewPagerTab = (SmartTabLayout) view.findViewById(R.id.viewpagertab);

        FragmentPagerItems pages = new FragmentPagerItems(view.getContext());
        pages.add(FragmentPagerItem.of("未取票", TicketHistoryNoTravel.class));
        pages.add(FragmentPagerItem.of("历史订单", TickHistoryTotal.class));

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(getFragmentManager(), pages);

        viewPager.setAdapter(adapter);
        viewPagerTab.setViewPager(viewPager);

        return view;
    }
}
