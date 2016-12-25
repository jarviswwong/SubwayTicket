package cn.mcavoy.www.subwayticket;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import cn.mcavoy.www.subwayticket.Application.MetroApplication;
import cn.mcavoy.www.subwayticket.Fragment.FragmentMain;
import cn.mcavoy.www.subwayticket.Fragment.FragmentTicketHistory;
import cn.mcavoy.www.subwayticket.Fragment.FragmentUserSetting;
import cn.mcavoy.www.subwayticket.Model.UserModel;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FragmentMain fragmentMain;
    private FragmentUserSetting fragmentUserSetting;
    private FragmentTicketHistory fragmentTicketHistory;

    private Fragment isFragment; //记录当前的fragment

    private Toolbar toolbar;

    private TextView navUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("首页");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        ViewGroup.LayoutParams params = navigationView.getLayoutParams();
        params.width = getResources().getDisplayMetrics().widthPixels * 7 / 10;
        navigationView.setLayoutParams(params);

        View navHeader = navigationView.getHeaderView(0);
        navUserName = (TextView) navHeader.findViewById(R.id.nav_username);
        navUserName.setText(MetroApplication.userModel.getName().toString());

        setDefaultFragment(savedInstanceState);
    }

    public void setDefaultFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            if (fragmentMain == null)
                fragmentMain = new FragmentMain();
            isFragment = fragmentMain;
            transaction.replace(R.id.fragment_layout, fragmentMain);
            transaction.commit();
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_index: {
                toolbar.setTitle("首页");
                if (fragmentMain == null)
                    fragmentMain = new FragmentMain();
                switchContent(isFragment, fragmentMain);
                break;
            }
            case R.id.nav_purchase_history: {
                toolbar.setTitle("购票记录");
                if (fragmentTicketHistory == null)
                    fragmentTicketHistory = new FragmentTicketHistory();
                switchContent(isFragment, fragmentTicketHistory);
                break;
            }
            case R.id.nav_user: {
                toolbar.setTitle("账户设置");
                if (fragmentUserSetting == null)
                    fragmentUserSetting = new FragmentUserSetting();
                switchContent(isFragment, fragmentUserSetting);
                break;
            }
            case R.id.nav_sign_out: {
                SharedPreferences sp = getSharedPreferences("user_validate", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.clear();
                editor.commit();
                MetroApplication.userModel = null;
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            }
            default:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //用更优化的方法来切换fragment
    private void switchContent(Fragment from, Fragment to) {
        if (isFragment != to) {
            isFragment = to;
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            if (!to.isAdded()) {
                ft.hide(from).add(R.id.fragment_layout, to).commit();
            } else {
                ft.hide(from).show(to).commit();
            }
        }
    }
}
