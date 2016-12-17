package cn.mcavoy.www.subwayticket.Adapter;

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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import cn.mcavoy.www.subwayticket.R;
import cn.mcavoy.www.subwayticket.subwayListModel.StationModel;
import cn.mcavoy.www.subwayticket.widget.IndexAdapter;


public class StationListAdapter extends BaseAdapter<StationModel.StationsEntity, StationListAdapter.ViewHolder>
        implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder>, IndexAdapter, View.OnClickListener {

    //当前处于打开状态的item
    private List<SwipeItemLayout> mOpenedSil = new ArrayList<>();
    private List<StationModel.StationsEntity> mLists;
    private Context mContext;

    //声明接口变量
    private OnRecyclerViewItemListener mOnItemClickListener = null;

    //这里重写OnClickListenerd的方法
    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v, (StationModel.StationsEntity) v.getTag());
        }
    }

    //定义一个点击事件的接口
    public static interface OnRecyclerViewItemListener {
        void onItemClick(View view, StationModel.StationsEntity stationsEntity);
    }

    //定义一个方法给外部用
    public void setmOnItemClickListener(OnRecyclerViewItemListener listener) {
        this.mOnItemClickListener = listener;
    }

    public StationListAdapter(Context ct, List<StationModel.StationsEntity> mLists) {
        this.mLists = mLists;
        mContext = ct;
        this.addAll(mLists);
    }

    @Override
    public StationListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.station_list_recylerview, parent, false);

        //添加点击事件
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StationListAdapter.ViewHolder holder, final int position) {
        TextView stationNameTextView = holder.mStationName;
        TextView metroLineTextView = holder.mMetroLine;
        TextView metroLineSecondTextView = holder.mMetroLineSecond;
        stationNameTextView.setText(getItem(position).getStationName());
        metroLineTextView.setText(getItem(position).getMetroLine() + "号线");
        if (!getItem(position).getMetroLineSecond().equals("")) {
            metroLineSecondTextView.setVisibility(View.VISIBLE);
            metroLineSecondTextView.setText(getItem(position).getMetroLineSecond() + "号线");
        }

        holder.itemView.setTag(mLists.get(position));
    }

    @Override
    public long getHeaderId(int position) {
        return getItem(position).getSortLetters().charAt(0);
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.station_list_header, viewGroup, false);
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
        public TextView mMetroLineSecond;

        public ViewHolder(View itemView) {
            super(itemView);
            mStationName = (TextView) itemView.findViewById(R.id.item_station_name);
            mMetroLine = (TextView) itemView.findViewById(R.id.item_station_metroLine);
            mMetroLineSecond = (TextView) itemView.findViewById(R.id.item_station_metroLineSecond);
        }
    }


}
