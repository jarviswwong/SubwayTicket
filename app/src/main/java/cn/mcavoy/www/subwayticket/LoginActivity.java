package cn.mcavoy.www.subwayticket;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.Priority;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.error.TimeoutError;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;

import java.util.Map;

import cn.mcavoy.www.subwayticket.Application.MetroApplication;
import cn.mcavoy.www.subwayticket.Model.UserModel;

public class LoginActivity extends AppCompatActivity {
    private Button signInButton;
    private MaterialEditText userNameEditText, userPassEditText;

    private Object cancelSign = new Object();

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private Gson gson;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences("user_validate", Context.MODE_PRIVATE);
        editor = sp.edit();
        gson = new Gson();

        //先进行登录验证
        if (sp.contains("isValidated")) {
            if (sp.getString("isValidated", null).equals("true"))
                InterfaceToMain();
        }
        setContentView(R.layout.login_main);

        signInButton = (Button) findViewById(R.id.btn_signIn);
        signInButton.setEnabled(false);
        signInButton.setOnClickListener(signInClick);

        userNameEditText = (MaterialEditText) findViewById(R.id.login_textField_username);
        userPassEditText = (MaterialEditText) findViewById(R.id.login_textField_pass);
        userNameEditText.addTextChangedListener(textWatcher);
        userPassEditText.addTextChangedListener(textWatcher);

    }

    //跳转类
    private void InterfaceToMain() {
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    //验证是否登陆过
//    public void validateLoginData() {
//        if (sp.contains("user_token") && sp.contains("user_info")) {
//            Log.d("user_token", sp.getString("user_token", null));
//            Log.d("user_msg", sp.getString("user_info", null));
//            Request<String> validateRequest = new StringRequest(getUserApi);
//            validateRequest.addHeader("Authorization", "Bearer " + sp.getString("user_token", null));
//            queue.add(0, validateRequest, new OnResponseListener<String>() {
//                @Override
//                public void onStart(int what) {
//
//                }
//
//                @Override
//                public void onSucceed(int what, Response<String> response) {
//                    if (what == 0) {
//                        if (response.responseCode() == 200) {
//                            editor.putString("user_info", response.get());
//                            editor.commit();
//                            InterfaceToMain();
//                        }
//                    }
//                }
//
//                @Override
//                public void onFailed(int what, Response<String> response) {
//
//                }
//
//                @Override
//                public void onFinish(int what) {
//
//                }
//            });
//        }
//    }

    View.OnClickListener signInClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String user_email = userNameEditText.getText().toString();
            String user_pass = userPassEditText.getText().toString();
            //申请token
            Request<String> tokenRequest = NoHttp.createStringRequest(MetroApplication.getTokenApi, RequestMethod.POST);
            tokenRequest.setPriority(Priority.HEIGHT);
            tokenRequest.add("username", user_email);
            tokenRequest.add("password", user_pass);
            tokenRequest.add("grant_type", "password");
            tokenRequest.add("client_id", MetroApplication.ClientId);
            tokenRequest.add("client_secret", MetroApplication.ClientSecret);
            CallServer.getInstance().add(1, tokenRequest, loginResponseListener);
        }
    };

    private OnResponseListener loginResponseListener = new OnResponseListener() {
        @Override
        public void onStart(int what) {

        }

        @Override
        public void onSucceed(int what, Response response) {
            if (what == 1) {
                if (response.responseCode() == 200) {
                    Map<String, Object> map = gson.fromJson(response.get().toString(), new TypeToken<Map<String, Object>>() {
                    }.getType());
                    String token = map.get("access_token").toString();
                    editor.putString("user_token", token);
                    editor.commit();
                    Request<String> requestForUser = NoHttp.createStringRequest(MetroApplication.getUserApi);
                    requestForUser.addHeader("Authorization", "Bearer " + sp.getString("user_token", null));
                    requestForUser.setCancelSign(cancelSign);
                    CallServer.getInstance().add(2, requestForUser, loginResponseListener);

                } else if (response.responseCode() == 401) {
                    Toast.makeText(LoginActivity.this, "用户名或密码错误!", Toast.LENGTH_SHORT).show();
                    CallServer.getInstance().cancelBySign(cancelSign);
                }
            }
            if (what == 2) {
                editor.putString("user_info", response.get().toString());
                editor.putString("isValidated", "true");
                editor.commit();
                MetroApplication.userModel = gson.fromJson(response.get().toString(), UserModel.class);
                InterfaceToMain();
            }
        }

        @Override
        public void onFailed(int what, Response response) {
            Exception exception = response.getException();

            if (exception instanceof TimeoutError) {
                Toast.makeText(getBaseContext(), "服务器连接超时！", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFinish(int what) {

        }
    };

    //没有输入值不可点击
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (userNameEditText.length() != 0 && userPassEditText.length() != 0) {
                signInButton.setEnabled(true);
            } else
                signInButton.setEnabled(false);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
}
