package cn.mcavoy.www.subwayticket.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Collection;
import java.util.List;

import cn.mcavoy.www.subwayticket.Model.TicketModel;
import cn.mcavoy.www.subwayticket.R;

public class TicketListAdapter extends RecyclerView.Adapter<TicketListAdapter.ViewHolder> implements View.OnClickListener {

    private List<TicketModel.TicketsEntity> list;
    private Context mContext;

    //声明接口变量
    private OnRecyclerViewItemListener mOnItemClickListener = null;

    //定义接口
    public static interface OnRecyclerViewItemListener {
        void onItemClick(View view, TicketModel.TicketsEntity ticketsEntity);
    }

    //定义一个方法给外部用
    public void setmOnItemClickListener(OnRecyclerViewItemListener listener) {
        this.mOnItemClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v, (TicketModel.TicketsEntity) v.getTag());
        }
    }

    public TicketListAdapter(Context context, List<TicketModel.TicketsEntity> list) {
        this.mContext = context;
        this.list = list;
        this.notifyDataSetChanged();
    }

    @Override
    public TicketListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ticket_list_recyclerview, parent, false);

        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TicketListAdapter.ViewHolder holder, int position) {
        holder.moStation.setText(list.get(position).getoStationName());
        holder.mtStation.setText(list.get(position).gettStationName());
        holder.mTicketNum.setText(list.get(position).getTicketNum());
        holder.mTicketPrice.setText(list.get(position).getTicketPrice());
        holder.mStatus.setText(list.get(position).getTicketStatus());
        if (holder.mStatus.getText().equals("未取票")) {
            holder.mStatus.setTextColor(mContext.getResources().getColor(R.color.colorRedForMoney));
        } else if (holder.mStatus.getText().equals("已取票")) {
            holder.mStatus.setTextColor(mContext.getResources().getColor(R.color.colorPrimary_hunt));
        } else {
            holder.mStatus.setTextColor(mContext.getResources().getColor(R.color.colorBlack));
        }

        holder.itemView.setTag(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView moStation;
        public TextView mtStation;
        public TextView mTicketNum;
        public TextView mTicketPrice;
        public TextView mStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            moStation = (TextView) itemView.findViewById(R.id.history_origin_station);
            mtStation = (TextView) itemView.findViewById(R.id.history_target_station);
            mTicketNum = (TextView) itemView.findViewById(R.id.history_ticket_number);
            mTicketPrice = (TextView) itemView.findViewById(R.id.history_ticket_price);
            mStatus = (TextView) itemView.findViewById(R.id.history_ticket_status);
        }
    }
}
