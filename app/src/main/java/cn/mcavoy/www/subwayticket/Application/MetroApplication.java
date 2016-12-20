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
    private RequestQueue queue = NoHttp.newRequestQueue(1);

    @Override
    public void onCreate() {
        super.onCreate();
        sp = getSharedPreferences("user_validate", Context.MODE_PRIVATE);
        editor = sp.edit();
        NoHttp.initialize(this, new NoHttp.Config()
                .setNetworkExecutor(new OkHttpNetworkExecutor())
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
            queue.add(0, validateRequest, new OnResponseListener<String>() {
                @Override
                public void onStart(int what) {

                }

                @Override
                public void onSucceed(int what, Response<String> response) {
                    if (what == 0) {
                        if (response.responseCode() == 200) {
                            editor.putString("user_info", response.get());
                            editor.putString("isValidated", "true");
                        } else {
                            editor.putString("isValidated", "false");
                        }
                        editor.commit();
                    }
                }

                @Override
                public void onFailed(int what, Response<String> response) {

                }

                @Override
                public void onFinish(int what) {

                }
            });
        }
    }
}
