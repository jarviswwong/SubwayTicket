package cn.mcavoy.www.subwayticket;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jiang.android.lib.adapter.expand.StickyRecyclerHeadersDecoration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import cn.mcavoy.www.subwayticket.Adapter.StationListAdapter;
import cn.mcavoy.www.subwayticket.Application.MetroApplication;
import cn.mcavoy.www.subwayticket.Pinyin.CharacterParser;
import cn.mcavoy.www.subwayticket.Pinyin.PinyinComparator;
import cn.mcavoy.www.subwayticket.subwayListModel.StationModel;
import cn.mcavoy.www.subwayticket.widget.DividerDecoration;
import cn.mcavoy.www.subwayticket.widget.SideBar;
import cn.mcavoy.www.subwayticket.widget.TouchableRecyclerView;
import cn.mcavoy.www.subwayticket.widget.ZSideBar;

public class StationListActivity extends AppCompatActivity {
    private CharacterParser characterParser;
    private PinyinComparator pinyinComparator;
    private SideBar mSideBar;
    private ZSideBar mZSideBar;
    private TextView mUserDialog;
    private TouchableRecyclerView mRecyclerView;

    private List<StationModel.StationsEntity> mStations = new ArrayList<>();
    private List<StationModel.StationsEntity> mAllLists = new ArrayList<>();

    //Adapter
    private StationListAdapter mAdapter;

    //Model
    public StationModel mModel;

    private TextView stationListTitle;

    //Choose Type 0:起点站 / 1:终点站
    private int chooseType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.station_list_main);
        initView();
        initToolbar();
        getDataFromActivity();
    }


    private void getDataFromActivity() {
        stationListTitle = (TextView) findViewById(R.id.station_list_title);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            chooseType = bundle.getInt("chooseType");
        }
        if (chooseType == 0) {
            stationListTitle.setText("选择出发站");
        }
        if (chooseType == 1) {
            stationListTitle.setText("选择到达站");
        }
    }

    private void initView() {
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();
        mSideBar = (SideBar) findViewById(R.id.station_siderbar);
        mZSideBar = (ZSideBar) findViewById(R.id.station_zsidebar);
        mUserDialog = (TextView) findViewById(R.id.station_dialog);
        mRecyclerView = (TouchableRecyclerView) findViewById(R.id.station_recycler_view);
        mSideBar.setTextView(mUserDialog);
        mZSideBar.setTextView(mUserDialog);
        getData();
    }

    public void getData() {
//        String tempData = "[{\"id\":\"123456\",\"stationName\":\"闸弄口\",\"metroLine\":\"1\",\"metroLineSecond\":\"\"}," +
//                "{\"id\":\"123333\",\"stationName\":\"火车东站\",\"metroLine\":\"1\",\"metroLineSecond\":\"4\"}," +
//                "{\"id\":\"32131\",\"stationName\":\"打铁关\",\"metroLine\":\"1\",\"metroLineSecond\":\"\"}," +
//                "{\"id\":\"2132131\",\"stationName\":\"九堡\",\"metroLine\":\"1\",\"metroLineSecond\":\"\"}," +
//                "{\"id\":\"2323\",\"stationName\":\"湘湖\",\"metroLine\":\"1\",\"metroLineSecond\":\"\"}," +
//                "{\"id\":\"2132131\",\"stationName\":\"江陵路\",\"metroLine\":\"1\",\"metroLineSecond\":\"\"}," +
//                "{\"id\":\"2132131\",\"stationName\":\"近江\",\"metroLine\":\"4\",\"metroLineSecond\":\"\"}," +
//                "{\"id\":\"2132131\",\"stationName\":\"龙翔桥\",\"metroLine\":\"1\",\"metroLineSecond\":\"\"}," +
//                "{\"id\":\"2132131\",\"stationName\":\"婺江路\",\"metroLine\":\"1\",\"metroLineSecond\":\"\"}]";

        String tempData = MetroApplication.tempData;
        if (tempData.equals(""))
            Toast.makeText(getBaseContext(), "读取车站数据失败，请检查网络设置!", Toast.LENGTH_SHORT).show();

        try {
            Gson gson = new GsonBuilder().create();
            List<StationModel.StationsEntity> stationsEntities = gson.fromJson(tempData, new TypeToken<List<StationModel.StationsEntity>>() {
            }.getType());
            mModel = new StationModel();
            mModel.setStationsEntities(stationsEntities);
            initUi();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initUi() {
        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = mAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mRecyclerView.getLayoutManager().scrollToPosition(position);
                }
            }
        });
        //use seperateLists model
        seperateLists(mModel);
        if (mAdapter == null) {
            mAdapter = new StationListAdapter(this, mAllLists);
            int orientation = LinearLayoutManager.VERTICAL;
            final LinearLayoutManager layoutManager = new LinearLayoutManager(this, orientation, false);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setAdapter(mAdapter);
            final StickyRecyclerHeadersDecoration headersDecor = new StickyRecyclerHeadersDecoration(mAdapter);
            mRecyclerView.addItemDecoration(headersDecor);
            mRecyclerView.addItemDecoration(new DividerDecoration(this));

            //   setTouchHelper();
            mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    headersDecor.invalidateHeaders();
                }
            });
        } else {
            mAdapter.notifyDataSetChanged();
        }
        //设置每一个item的点击事件
        mAdapter.setmOnItemClickListener(new StationListAdapter.OnRecyclerViewItemListener() {
            @Override
            public void onItemClick(View view, StationModel.StationsEntity stationsEntity) {
                Intent intent = new Intent();
                intent.setClass(StationListActivity.this, MainActivity.class);
                intent.putExtra("StationName", stationsEntity.getStationName());
                setResult(chooseType, intent);
                finish();
            }
        });
        mZSideBar.setupWithRecycler(mRecyclerView);
    }

    private void seperateLists(StationModel mModel) {
        //stations;
        if (mModel.getStationsEntities() != null && mModel.getStationsEntities().size() > 0) {
            for (int i = 0; i < mModel.getStationsEntities().size(); i++) {
                StationModel.StationsEntity entity = new StationModel.StationsEntity();
                entity.setId(mModel.getStationsEntities().get(i).getId());
                entity.setStationName(mModel.getStationsEntities().get(i).getStationName());
                entity.setMetroLine(mModel.getStationsEntities().get(i).getMetroLine());
                entity.setMetroLineSecond(mModel.getStationsEntities().get(i).getMetroLineSecond());
                String pinyin = characterParser.getSelling(mModel.getStationsEntities().get(i).getStationName());
                String sortString = pinyin.substring(0, 1).toUpperCase();

                if (sortString.matches("[A-Z]")) {
                    entity.setSortLetters(sortString.toUpperCase());
                } else {
                    entity.setSortLetters("#");
                }
                mStations.add(entity);
            }
            Collections.sort(mStations, pinyinComparator);
            mAllLists.addAll(mStations);
        }
    }

    //初始化标题栏
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.origin_station_list_toolbar);
        toolbar.setNavigationIcon(R.mipmap.btn_back);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        //navigation onclick Listener
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(-1, intent);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.station_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return false;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(-1, intent);
        finish();
    }
}
