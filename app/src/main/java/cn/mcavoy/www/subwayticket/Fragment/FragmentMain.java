package cn.mcavoy.www.subwayticket.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.telecom.Call;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.google.gson.Gson;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.error.TimeoutError;
import com.yolanda.nohttp.rest.CacheMode;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.RequestQueue;
import com.yolanda.nohttp.rest.Response;
import com.yolanda.nohttp.rest.StringRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import cn.mcavoy.www.subwayticket.Application.MetroApplication;
import cn.mcavoy.www.subwayticket.CallServer;
import cn.mcavoy.www.subwayticket.MainActivity;
import cn.mcavoy.www.subwayticket.R;
import cn.mcavoy.www.subwayticket.StationListActivity;

import static com.yolanda.nohttp.rest.CacheMode.REQUEST_NETWORK_FAILED_READ_CACHE;


public class FragmentMain extends Fragment {
    private View originLayout, targetLayout;
    private TextView originText, targetText, ticketNum, ticketPay;
    private Button ticketAdd, ticketCut, bookTicket;
    private FragmentTicketHistory fragmentTicketHistory;

    private String originStationName = "请选择", targetStationName = "请选择";  //记录用户选择
    private String tempPay = "0";

    private View view;
    private CircularProgressButton circularProgressButton;
    private DialogPlus dialogPlus;

    private Fragment fragment;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.content_main, container, false);
        fragment = this;  //当前fragment
        navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

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

        //点击购票按钮
        bookTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConfirmDialog();
            }
        });

        return view;
    }

    //弹出购票对话框
    private void startConfirmDialog() {
        dialogPlus = DialogPlus.newDialog(view.getContext())
                .setContentHolder(new ViewHolder(R.layout.confirm_dialog_view))
                .setGravity(Gravity.BOTTOM)
                .setCancelable(true)
                .setHeader(R.layout.confirm_dialog_header)
                .create();

        View headerView = dialogPlus.getHeaderView();
        Button cancelDialog = (Button) headerView.findViewById(R.id.btn_cancelDialog);
        cancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogPlus.dismiss();
            }
        });

        View holderView = dialogPlus.getHolderView();
        TextView dialog_oStation = (TextView) holderView.findViewById(R.id.dialog_oStationName);
        TextView dialog_tStation = (TextView) holderView.findViewById(R.id.dialog_tStationName);
        TextView dialog_price = (TextView) holderView.findViewById(R.id.dialog_price);
        dialog_oStation.setText(originStationName);
        dialog_tStation.setText(targetStationName);
        dialog_price.setText(ticketPay.getText().toString());

        circularProgressButton = (CircularProgressButton) holderView.findViewById(R.id.btn_confirm_pay);
        circularProgressButton.setIndeterminateProgressMode(true);
        //提交购票按钮事件点击
        circularProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (circularProgressButton.getProgress() == 0) {
                    SharedPreferences sp = getActivity().getSharedPreferences("user_validate", Context.MODE_PRIVATE);
                    //假如存在
                    if (sp.contains("user_token")) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date curDate = new Date(System.currentTimeMillis());
                        String thisDate = simpleDateFormat.format(curDate);
                        Request<String> submitRequest = new StringRequest(MetroApplication.postSubmitTicketApi, RequestMethod.POST);
                        submitRequest.addHeader("Authorization", "Bearer " + sp.getString("user_token", null));
                        submitRequest.add("ownerId", MetroApplication.userModel.getId());
                        submitRequest.add("oStationName", originStationName);
                        submitRequest.add("tStationName", targetStationName);
                        submitRequest.add("ticketNum", ticketNum.getText().toString());
                        submitRequest.add("ticketPrice", ticketPay.getText().toString());
                        submitRequest.add("ticketStatus", "未取票");
                        submitRequest.add("payDate", thisDate);
                        CallServer.getInstance().add(2, submitRequest, listener);
                    } else {
                        Toast.makeText(getActivity().getBaseContext(), "请重新登录!", Toast.LENGTH_SHORT).show();
                    }
                } else if (circularProgressButton.getProgress() == -1) {
                    circularProgressButton.setProgress(0);
                } else if (circularProgressButton.getProgress() == 100) {
                    dialogPlus.dismiss();
                }
            }
        });

        dialogPlus.show();
    }

    //textview监听事件
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

    public void returnToTicketHistory() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialogPlus.dismiss();
                        navigationView.getMenu().getItem(1).setChecked(true);
                        toolbar.setTitle("购票记录");
                        FragmentManager fm = getFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        if (!MainActivity.fragmentTicketHistory.isAdded()) {
                            ft.hide(fragment).add(R.id.fragment_layout, MainActivity.fragmentTicketHistory).commit();
                        } else {
                            ft.hide(fragment).show(MainActivity.fragmentTicketHistory).commit();
                        }
                        MainActivity.isFragment = MainActivity.fragmentTicketHistory;
                        TicketHistoryNoTravel.autorefresh();
                    }
                });
            }
        };
        timer.schedule(task, 2000);
    }

    private void getStationList() {
        Request<String> listRequest = new StringRequest(MetroApplication.getStationApi);
        listRequest.setCacheMode(REQUEST_NETWORK_FAILED_READ_CACHE);
        CallServer.getInstance().add(0, listRequest, listener);
    }

    OnResponseListener listener = new OnResponseListener() {
        @Override
        public void onStart(int what) {
            if (what == 2) {
                circularProgressButton.setProgress(50);
            }
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
            if (what == 2) {
                if (response.responseCode() == 200) {
                    circularProgressButton.setProgress(100);
                    Toast.makeText(view.getContext(), "购票成功！正在跳转到订单界面...", Toast.LENGTH_SHORT).show();
                    returnToTicketHistory();
                } else {
                    circularProgressButton.setProgress(-1);
                }
            }
        }

        @Override
        public void onFailed(int what, Response response) {
            Exception exception = response.getException();

            if (exception instanceof TimeoutError) {
                if (what == 0) {
                    getStationList();
                }
                if (what == 1) {
                    Toast.makeText(getActivity().getBaseContext(), "error: 服务器连接失败", Toast.LENGTH_SHORT).show();
                }
                if (what == 2) {
                    circularProgressButton.setProgress(-1);
                }
            }

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
                Toast.makeText(getActivity().getBaseContext(), "消息功能还没写，别点啦！", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }


}
