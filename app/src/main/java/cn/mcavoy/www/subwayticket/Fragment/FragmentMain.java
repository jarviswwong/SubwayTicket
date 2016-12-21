package cn.mcavoy.www.subwayticket.Fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.rest.CacheMode;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.RequestQueue;
import com.yolanda.nohttp.rest.Response;
import com.yolanda.nohttp.rest.StringRequest;

import cn.mcavoy.www.subwayticket.Application.MetroApplication;
import cn.mcavoy.www.subwayticket.R;
import cn.mcavoy.www.subwayticket.StationListActivity;


public class FragmentMain extends Fragment {
    private View originLayout, targetLayout;
    private TextView originText, targetText, ticketNum;
    private Button ticketAdd, ticketCut, bookTicket;

    String originStationName = "请选择", targetStationName = "请选择";  //记录用户选择

    //http
    private String getStationApi = "http://10.0.2.2/api/stations";
    private RequestQueue queue;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_main, container, false);
        originLayout = view.findViewById(R.id.content_main_originLayout);
        targetLayout = view.findViewById(R.id.content_main_targetLayout);
        originText = (TextView) view.findViewById(R.id.textView_originText);
        targetText = (TextView) view.findViewById(R.id.textView_targetText);
        ticketNum = (TextView) view.findViewById(R.id.ticket_number);
        ticketAdd = (Button) view.findViewById(R.id.ticket_num_add);
        ticketCut = (Button) view.findViewById(R.id.ticket_num_cut);
        bookTicket = (Button) view.findViewById(R.id.button_startBookTicket);

        //提前初始化站台list缓存 加快加载速度
        queue = NoHttp.newRequestQueue();
        getStationList();

        setHasOptionsMenu(true);
        //出发站选择击事件
        originLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), StationListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("chooseType", 0);
                intent.putExtras(bundle);
                startActivityForResult(intent, 0);
            }
        });

        //到达站点击事件
        targetLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), StationListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("chooseType", 1);
                intent.putExtras(bundle);
                startActivityForResult(intent, 1);
            }
        });

        //增加票数
        ticketAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int number = Integer.parseInt(ticketNum.getText().toString());
                ticketNum.setText(String.valueOf(++number));
            }
        });

        //减少票数
        ticketCut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ticketNum.getText().toString().equals("1")) {
                    Toast.makeText(getActivity().getBaseContext(), "至少购买一张票!", Toast.LENGTH_SHORT).show();
                } else {
                    int number = Integer.parseInt(ticketNum.getText().toString());
                    ticketNum.setText(String.valueOf(--number));
                }
            }
        });

        bookTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
    }

    private void getStationList() {
        Request<String> listRequest = new StringRequest(getStationApi);
        listRequest.setCacheMode(CacheMode.REQUEST_NETWORK_FAILED_READ_CACHE);
        queue.add(0, listRequest, listener);
    }

    OnResponseListener listener = new OnResponseListener() {
        @Override
        public void onStart(int what) {

        }

        @Override
        public void onSucceed(int what, Response response) {
            if (what == 0) {
                if (response.responseCode() == 200) {
                    MetroApplication.tempData = response.get().toString();
                }
            }
        }

        @Override
        public void onFailed(int what, Response response) {

        }

        @Override
        public void onFinish(int what) {

        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case -1: {
                break;
            }
            case 0: {
                Bundle b = data.getExtras();
                originStationName = b.getString("StationName");
                originText.setText(originStationName);
                break;
            }
            case 1: {
                Bundle b = data.getExtras();
                targetStationName = b.getString("StationName");
                targetText.setText(targetStationName);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.subway_toolbar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_infomation: {
                Toast.makeText(getActivity().getBaseContext(), "ring here", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
