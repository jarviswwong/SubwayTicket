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
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class OriginStationListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerViewAdapter MyAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.origin_station_list_main);
        initToolbar();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        MyAdapter = new RecyclerViewAdapter(OriginStationListActivity.this, initData());
        recyclerView.setAdapter(MyAdapter);
    }

    private List<String> initData() {
        List<String> datas = new ArrayList<String>();
        for (int i = 0; i <= 20; i++) {
            datas.add("item:" + i);
        }
        return datas;
    }

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
}
