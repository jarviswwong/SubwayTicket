package cn.mcavoy.www.subwayticket.Application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.yanzhenjie.nohttp.OkHttpNetworkExecutor;
import com.yolanda.nohttp.Logger;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.cache.DBCacheStore;
import com.yolanda.nohttp.cache.DiskCacheStore;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.RequestQueue;
import com.yolanda.nohttp.rest.Response;
import com.yolanda.nohttp.rest.StringRequest;

import java.util.Map;
import java.util.Objects;

import cn.mcavoy.www.subwayticket.MainActivity;


public class MetroApplication extends Application {
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private String getUserApi = "http://10.0.2.2/api/user";
    private RequestQueue queue;

    //设置全局变量保存站台数据
    public static String tempData = "";

    @Override
    public void onCreate() {
        super.onCreate();
        queue = NoHttp.newRequestQueue(2);
        sp = getSharedPreferences("user_validate", Context.MODE_PRIVATE);
        editor = sp.edit();
        NoHttp.initialize(this, new NoHttp.Config()
                .setNetworkExecutor(new OkHttpNetworkExecutor())
                .setCacheStore(
                        new DBCacheStore(this).setEnable(true)
                )
        );

        Logger.setDebug(true);
        Logger.setTag("MetroHttpDebugger");

        validateLoginData();
    }

    //验证是否登陆过
    public void validateLoginData() {
        if (sp.contains("user_token") && sp.contains("user_info")) {
            Request<String> validateRequest = new StringRequest(getUserApi);
            validateRequest.addHeader("Authorization", "Bearer " + sp.getString("user_token", null));
            queue.add(0, validateRequest, listener);
        }
    }

    OnResponseListener listener = new OnResponseListener() {
        @Override
        public void onStart(int what) {

        }

        @Override
        public void onSucceed(int what, Response response) {
            if (what == 0) {
                if (response.responseCode() == 200) {
                    editor.putString("user_info", response.get().toString());
                    editor.putString("isValidated", "true");
                } else {
                    editor.putString("isValidated", "false");
                }
                editor.commit();
            }
        }

        @Override
        public void onFailed(int what, Response response) {

        }

        @Override
        public void onFinish(int what) {

        }
    };
}
