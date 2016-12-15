package cn.mcavoy.www.subwayticket;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jiang.android.lib.adapter.BaseAdapter;
import com.jiang.android.lib.adapter.expand.StickyRecyclerHeadersAdapter;
import com.jiang.android.lib.widget.SwipeItemLayout;

import java.util.ArrayList;
import java.util.List;

import cn.mcavoy.www.subwayticket.subwayListModel.StationModel;
import cn.mcavoy.www.subwayticket.widget.IndexAdapter;


public class OriginStationListAdapter extends BaseAdapter<StationModel.StationsEntity, OriginStationListAdapter.ViewHolder>
        implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder>, IndexAdapter {

    //当前处于打开状态的item
    private List<SwipeItemLayout> mOpenedSil = new ArrayList<>();
    private List<StationModel.StationsEntity> mLists;
    private Context mContext;

    public OriginStationListAdapter(Context ct, List<StationModel.StationsEntity> mLists) {
        this.mLists = mLists;
        mContext = ct;
        this.addAll(mLists);
    }

    @Override
    public OriginStationListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.origin_station_list_recylerview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OriginStationListAdapter.ViewHolder holder, final int position) {
        TextView stationNameTextView = holder.mStationName;
        TextView metroLineTextView = holder.mMetroLine;
        stationNameTextView.setText(getItem(position).getStationName());
        metroLineTextView.setText(getItem(position).getMetroLine() + "号线");
    }

    @Override
    public long getHeaderId(int position) {
        return getItem(position).getSortLetters().charAt(0);
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.origin_station_list_header, viewGroup, false);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        TextView textView = (TextView) viewHolder.itemView;
        String showValue = String.valueOf(getItem(position).getSortLetters().charAt(0));
        textView.setText(showValue);
    }

    public int getPositionForSection(char section) {
        for (int i = 0; i < getItemCount(); i++) {
            String sortStr = mLists.get(i).getSortLetters();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;

    }

    public void closeOpenedSwipeItemLayoutWithAnim() {
        for (SwipeItemLayout sil : mOpenedSil) {
            sil.closeWithAnim();
        }
        mOpenedSil.clear();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mStationName;
        public TextView mMetroLine;

        public ViewHolder(View itemView) {
            super(itemView);
            mStationName = (TextView) itemView.findViewById(R.id.item_station_name);
            mMetroLine = (TextView) itemView.findViewById(R.id.item_station_metroLine);
        }
    }


}
