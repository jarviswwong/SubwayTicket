package cn.mcavoy.www.subwayticket.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.telecom.Call;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.CacheMode;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.RequestQueue;
import com.yolanda.nohttp.rest.Response;
import com.yolanda.nohttp.rest.StringRequest;

import java.util.List;

import cn.mcavoy.www.subwayticket.Application.MetroApplication;
import cn.mcavoy.www.subwayticket.CallServer;
import cn.mcavoy.www.subwayticket.R;
import cn.mcavoy.www.subwayticket.StationListActivity;

import static com.yolanda.nohttp.rest.CacheMode.REQUEST_NETWORK_FAILED_READ_CACHE;


public class FragmentMain extends Fragment {
    private View originLayout, targetLayout;
    private TextView originText, targetText, ticketNum, ticketPay;
    private Button ticketAdd, ticketCut, bookTicket;

    String originStationName = "请选择", targetStationName = "请选择";  //记录用户选择
    String tempPay = "0";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_main, container, false);
        originLayout = view.findViewById(R.id.content_main_originLayout);
        targetLayout = view.findViewById(R.id.content_main_targetLayout);
        originText = (TextView) view.findViewById(R.id.textView_originText);
        targetText = (TextView) view.findViewById(R.id.textView_targetText);
        ticketNum = (TextView) view.findViewById(R.id.ticket_number);
        ticketPay = (TextView) view.findViewById(R.id.textView_payable);
        ticketAdd = (Button) view.findViewById(R.id.ticket_num_add);
        ticketCut = (Button) view.findViewById(R.id.ticket_num_cut);
        bookTicket = (Button) view.findViewById(R.id.button_startBookTicket);
        bookTicket.setEnabled(false);

        //提前初始化站台list缓存 加快加载速度
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

        originText.addTextChangedListener(textWatcher);
        targetText.addTextChangedListener(textWatcher);

        //票数变化监听
        ticketNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!ticketPay.getText().equals("0")) {
                    String resultPay = String.valueOf(Integer.parseInt(tempPay) * (Integer.parseInt(s.toString())));
                    ticketPay.setText(resultPay);
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

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!originStationName.equals("请选择") && !targetStationName.equals("请选择")) {
                //确保起点终点选择不同
                if (!originStationName.equals(targetStationName)) {
                    Request<String> fareRequest = new StringRequest(MetroApplication.getFaremapApi, RequestMethod.POST);
                    fareRequest.setCacheMode(REQUEST_NETWORK_FAILED_READ_CACHE);
                    fareRequest.add("originStation", originStationName);
                    fareRequest.add("targetStation", targetStationName);
                    CallServer.getInstance().add(1, fareRequest, listener);
                } else {
                    ticketPay.setText("0");
                    tempPay = "0";
                    bookTicket.setEnabled(false);
                    Toast.makeText(getActivity().getBaseContext(), "亲，您真的是来乘地铁的吗？", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private void getStationList() {
        Request<String> listRequest = new StringRequest(MetroApplication.getStationApi);
        listRequest.setCacheMode(REQUEST_NETWORK_FAILED_READ_CACHE);
        CallServer.getInstance().add(0, listRequest, listener);
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
            if (what == 1) {
                if (response.responseCode() == 200) {
                    ticketPay.setText("" + Integer.parseInt(response.get().toString()) * Integer.parseInt(ticketNum.getText().toString()));
                    tempPay = response.get().toString();
                    if (!ticketPay.getText().equals("0") || !ticketPay.getText().equals("")) {
                        bookTicket.setEnabled(true);
                    }
                }
                if (response.responseCode() == 500) {
                    Toast.makeText(getActivity().getBaseContext(), "目前还不支持换乘购票哦", Toast.LENGTH_SHORT).show();
                    ticketPay.setText("0");
                    tempPay = "0";
                    bookTicket.setEnabled(false);
                }
            }
        }

        @Override
        public void onFailed(int what, Response response) {
            Toast.makeText(getActivity().getBaseContext(), "获取票价失败：服务器连接失败！", Toast.LENGTH_SHORT).show();
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
