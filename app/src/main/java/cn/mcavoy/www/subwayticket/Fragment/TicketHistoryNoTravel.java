package cn.mcavoy.www.subwayticket.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.encoder.QRCode;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.victor.loading.rotate.RotateLoading;
import com.yolanda.nohttp.error.TimeoutError;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;
import com.yolanda.nohttp.rest.StringRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.mcavoy.www.subwayticket.Adapter.TicketListAdapter;
import cn.mcavoy.www.subwayticket.Application.MetroApplication;
import cn.mcavoy.www.subwayticket.CallServer;
import cn.mcavoy.www.subwayticket.Model.TicketModel;
import cn.mcavoy.www.subwayticket.R;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

public class TicketHistoryNoTravel extends Fragment {

    private RecyclerView mrecyclerView;
    private TicketModel ticketModel;
    private List<TicketModel.TicketsEntity> mlists = new ArrayList<>();
    private List<TicketModel.TicketsEntity> mAlllists = new ArrayList<>();
    private TicketListAdapter ticketListAdapter;

    private View view;
    private RelativeLayout noDateView;
    private RotateLoading rotateLoading;
    private DialogPlus ticketDetailsDialog;
    private TextView details_oStationName, details_tStationName, details_price, details_number, details_date;
    private ImageView qrCodeImage;

    public static PtrClassicFrameLayout ptrClassicFrameLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.ticket_history_notravel, container, false);
        noDateView = (RelativeLayout) view.findViewById(R.id.no_history_tip);
        mrecyclerView = (RecyclerView) view.findViewById(R.id.notravel_recycler_view);
        rotateLoading = (RotateLoading) view.findViewById(R.id.noTravelOrder_loading);
        ptrClassicFrameLayout = (PtrClassicFrameLayout) view.findViewById(R.id.notravel_history_refresh);
        getDate();

        ptrClassicFrameLayout.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                ptrClassicFrameLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getDate();
                    }
                }, 1800);
            }
        });

        //初始化订单细节布局
        startTicketDetials();
        return view;
    }

    //自动刷新界面
    public static void autorefresh() {
        if (ptrClassicFrameLayout != null) {
            ptrClassicFrameLayout.autoRefresh();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);


    }

    private void getDate() {
        SharedPreferences sp = getActivity().getSharedPreferences("user_validate", Context.MODE_PRIVATE);
        //假如存在
        if (sp.contains("user_token")) {
            Request<String> request = new StringRequest(MetroApplication.getNoTravelOrderApi);
            request.addHeader("ownerId", MetroApplication.userModel.getId());
            request.addHeader("Authorization", "Bearer " + sp.getString("user_token", null));
            CallServer.getInstance().add(0, request, listener);
        } else {

        }
    }

    OnResponseListener listener = new OnResponseListener() {
        @Override
        public void onStart(int what) {
            rotateLoading.start();
        }

        @Override
        public void onSucceed(int what, Response response) {
            if (what == 0) {
                if (response.responseCode() == 200) {
                    rotateLoading.stop();
                    String tempDate = response.get().toString();
                    if (!tempDate.equals("[]")) {
                        noDateView.setVisibility(View.GONE);
                    } else {
                        noDateView.setVisibility(View.VISIBLE);
                    }
                    try {
                        Gson gson = new Gson();
                        List<TicketModel.TicketsEntity> ticketsEntities = gson.fromJson(tempDate, new TypeToken<List<TicketModel.TicketsEntity>>() {
                        }.getType());
                        ticketModel = new TicketModel();
                        ticketModel.setTicketsEntities(ticketsEntities);
                        initUi();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void onFailed(int what, Response response) {
            Exception exception = response.getException();
            if (exception instanceof TimeoutError) {
                if (what == 0) {
                    Toast.makeText(getActivity().getBaseContext(), "服务器连接失败!", Toast.LENGTH_SHORT).show();
                    rotateLoading.stop();
                }
            }
        }

        @Override
        public void onFinish(int what) {
            ptrClassicFrameLayout.refreshComplete();
        }
    };

    private void initUi() {
        seperateLists(ticketModel);
        if (ticketListAdapter == null) {
            ticketListAdapter = new TicketListAdapter(view.getContext(), mAlllists);
            int orientation = LinearLayoutManager.VERTICAL;
            LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext(), orientation, false);
            mrecyclerView.setLayoutManager(layoutManager);
            mrecyclerView.setHasFixedSize(true);
            mrecyclerView.setAdapter(ticketListAdapter);
        } else {
            ticketListAdapter.notifyDataSetChanged();
        }

        ticketListAdapter.setmOnItemClickListener(new TicketListAdapter.OnRecyclerViewItemListener() {
            @Override
            public void onItemClick(View view, TicketModel.TicketsEntity ticketsEntity) {
                details_oStationName.setText(ticketsEntity.getoStationName());
                details_tStationName.setText(ticketsEntity.gettStationName());
                details_price.setText(ticketsEntity.getTicketPrice() + ".00");
                details_number.setText(ticketsEntity.getTicketNum());
                details_date.setText(ticketsEntity.getPayDate());
                //生成二维码暂时规则（唯一id,归属id,日期和状态中间用|隔开）
                Bitmap bitmap = generateBitmap(ticketsEntity.getId() + "|" + ticketsEntity.getOwnerId() + "|" + ticketsEntity.getPayDate() +
                        "|" + ticketsEntity.getTicketStatus(), 200, 200);
                qrCodeImage.setImageBitmap(bitmap);
                ticketDetailsDialog.show();
            }
        });
    }

    //二维码创造类
    private Bitmap generateBitmap(String content, int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, String> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        try {
            BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (encode.get(j, i)) {
                        pixels[i * width + j] = 0x00000000;
                    } else {
                        pixels[i * width + j] = 0xffffffff;
                    }
                }
            }
            return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void seperateLists(TicketModel model) {
        mlists.clear();
        mAlllists.clear();
        if (model.getTicketsEntities() != null && model.getTicketsEntities().size() > 0) {
            for (int i = 0; i < model.getTicketsEntities().size(); ++i) {
                TicketModel.TicketsEntity entity = new TicketModel.TicketsEntity();
                entity.setId(model.getTicketsEntities().get(i).getId());
                entity.setoStationName(model.getTicketsEntities().get(i).getoStationName());
                entity.settStationName(model.getTicketsEntities().get(i).gettStationName());
                entity.setOwnerId(model.getTicketsEntities().get(i).getOwnerId());
                entity.setTicketNum(model.getTicketsEntities().get(i).getTicketNum());
                entity.setTicketPrice(model.getTicketsEntities().get(i).getTicketPrice());
                entity.setTicketStatus(model.getTicketsEntities().get(i).getTicketStatus());
                entity.setPayDate(model.getTicketsEntities().get(i).getPayDate());
                mlists.add(entity);
            }
            mAlllists.addAll(mlists);
        }
    }

    private void startTicketDetials() {
        ticketDetailsDialog = DialogPlus.newDialog(view.getContext())
                .setContentHolder(new ViewHolder(R.layout.dialog_ticket_details))
                .setGravity(Gravity.CENTER)
                .setCancelable(true)
                .create();
        View ticketDetailsView = ticketDetailsDialog.getHolderView();
        details_oStationName = (TextView) ticketDetailsView.findViewById(R.id.ticket_details_oStationName);
        details_tStationName = (TextView) ticketDetailsView.findViewById(R.id.ticket_details_tStationName);
        details_price = (TextView) ticketDetailsView.findViewById(R.id.ticket_details_price);
        details_number = (TextView) ticketDetailsView.findViewById(R.id.ticket_details_number);
        details_date = (TextView) ticketDetailsView.findViewById(R.id.ticket_details_payDate);
        qrCodeImage = (ImageView) ticketDetailsView.findViewById(R.id.QRcode);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
