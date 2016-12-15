package cn.mcavoy.www.subwayticket;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jiang.android.lib.adapter.expand.StickyRecyclerHeadersDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.mcavoy.www.subwayticket.Pinyin.CharacterParser;
import cn.mcavoy.www.subwayticket.Pinyin.PinyinComparator;
import cn.mcavoy.www.subwayticket.subwayListModel.StationModel;
import cn.mcavoy.www.subwayticket.widget.DividerDecoration;
import cn.mcavoy.www.subwayticket.widget.SideBar;
import cn.mcavoy.www.subwayticket.widget.TouchableRecyclerView;
import cn.mcavoy.www.subwayticket.widget.ZSideBar;

public class OriginStationListActivity extends AppCompatActivity {
    private CharacterParser characterParser;
    private PinyinComparator pinyinComparator;
    private SideBar mSideBar;
    private ZSideBar mZSideBar;
    private TextView mUserDialog;
    private TouchableRecyclerView mRecyclerView;

    private List<StationModel.StationsEntity> mStations = new ArrayList<>();
    private List<StationModel.StationsEntity> mAllLists = new ArrayList<>();

    //Adapter
    private OriginStationListAdapter mAdapter;

    //Model
    public StationModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.origin_station_list_main);
        initView();
        initToolbar();
    }

    private void initView() {
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();
        mSideBar = (SideBar) findViewById(R.id.station_siderbar);
        mZSideBar = (ZSideBar) findViewById(R.id.station_zsidebar);
        mUserDialog = (TextView) findViewById(R.id.station_dialog);
        mRecyclerView = (TouchableRecyclerView) findViewById(R.id.station_recycler_view);
        mSideBar.setTextView(mUserDialog);
        getData();
    }

    public void getData() {
        String tempData = "[{\"id\":\"123456\",\"stationName\":\"闸弄口\",\"metroLine\":\"1\"},{\"id\":\"123333\",\"stationName\":\"火车东站西\",\"metroLine\":\"1\"},{\"id\":\"32131\",\"stationName\":\"打铁关\",\"metroLine\":\"1\"},{\"id\":\"2132131\",\"stationName\":\"九堡\",\"metroLine\":\"1\"}]";

        try {
            Gson gson = new Gson();
            List<StationModel.StationsEntity> stationsEntities = gson.fromJson(tempData,new TypeToken<List<StationModel.StationsEntity>>(){}.getType());
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
            mAdapter = new OriginStationListAdapter(this, mAllLists);
            int orientation = LinearLayoutManager.VERTICAL;
            final LinearLayoutManager layoutManager = new LinearLayoutManager(this, orientation, false);
            mRecyclerView.setLayoutManager(layoutManager);
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
                intent.setClass(OriginStationListActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.origin_station_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_zsidebar) {
            mZSideBar.setVisibility(View.VISIBLE);
            mSideBar.setVisibility(View.GONE);
        } else {
            mZSideBar.setVisibility(View.GONE);
            mSideBar.setVisibility(View.VISIBLE);
        }
        return false;
    }
}
