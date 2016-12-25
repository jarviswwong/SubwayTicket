package cn.mcavoy.www.subwayticket;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.Priority;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.error.TimeoutError;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;
import com.yolanda.nohttp.rest.StringRequest;

import java.util.Map;

import cn.mcavoy.www.subwayticket.Application.MetroApplication;
import cn.mcavoy.www.subwayticket.Model.UserModel;

public class LoginActivity extends AppCompatActivity {
    private Button signInButton;
    private MaterialEditText userNameEditText, userPassEditText;
    private TextView turnToSignUp;
    private Object cancelSign = new Object();

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private Gson gson;

    private DialogPlus dialogPlus, loadingUserDateDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences("user_validate", Context.MODE_PRIVATE);
        editor = sp.edit();
        gson = new Gson();
        //去掉状态栏
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        Window window = this.getWindow();
        window.setFlags(flag, flag);

        validateLoginData();
    }

    //显示登录的界面方法
    private void ShowLoginView() {
        setContentView(R.layout.login_main);

        signInButton = (Button) findViewById(R.id.btn_signIn);
        signInButton.setEnabled(false);
        signInButton.setOnClickListener(signInClick);

        userNameEditText = (MaterialEditText) findViewById(R.id.login_textField_username);
        userPassEditText = (MaterialEditText) findViewById(R.id.login_textField_pass);
        userNameEditText.addTextChangedListener(textWatcher);
        userPassEditText.addTextChangedListener(textWatcher);

        turnToSignUp = (TextView) findViewById(R.id.turnTo_signUp);
        turnToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    //验证是否登陆过
    public void validateLoginData() {
        if (sp.contains("user_token") && sp.contains("user_info")) {
            Request<String> validateRequest = new StringRequest(MetroApplication.getUserApi);
            validateRequest.addHeader("Authorization", "Bearer " + sp.getString("user_token", null));
            CallServer.getInstance().add(0, validateRequest, listener);
        } else {
            ShowLoginView();
        }
    }

    //监听获取用户的http请求
    OnResponseListener listener = new OnResponseListener() {
        @Override
        public void onStart(int what) {
            startloadingUserDateDialog();
            loadingUserDateDialog.show();
        }

        @Override
        public void onSucceed(int what, Response response) {
            if (what == 0) {
                if (response.responseCode() == 200) {
                    editor.putString("user_info", response.get().toString());
                    editor.putString("isValidated", "true");
                    Gson gson = new Gson();
                    MetroApplication.userModel = gson.fromJson(response.get().toString(), UserModel.class);
                    InterfaceToMain();
                } else {
                    editor.putString("isValidated", "false");
                    MetroApplication.userModel = null;
                    ShowLoginView();
                }
                editor.commit();
            }
        }

        @Override
        public void onFailed(int what, Response response) {
            Exception exception = response.getException();

            if (exception instanceof TimeoutError) {
                MetroApplication.userModel = null;
                editor.clear();
                editor.commit();
                ShowLoginView();
            }
        }

        @Override
        public void onFinish(int what) {
            loadingUserDateDialog.dismiss();
        }
    };

    //读取数据loading动画
    private void startloadingUserDateDialog() {
        loadingUserDateDialog = DialogPlus.newDialog(LoginActivity.this)
                .setContentHolder(new ViewHolder(R.layout.dialog_loading_userdate))
                .setOverlayBackgroundResource(android.R.color.transparent)
                .setContentBackgroundResource(Color.TRANSPARENT)
                .setGravity(Gravity.CENTER)
                .setCancelable(false)
                .create();
    }

    //跳转到主类
    private void InterfaceToMain() {
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

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

    //登录加载动画初始化
    public void startAnimateLoading() {
        dialogPlus = DialogPlus.newDialog(LoginActivity.this)
                .setContentHolder(new ViewHolder(R.layout.dialog_loading_two))
                .setContentBackgroundResource(Color.TRANSPARENT)
                .setGravity(Gravity.CENTER)
                .setCancelable(false)
                .create();
    }

    //登录请求http监听
    private OnResponseListener loginResponseListener = new OnResponseListener() {
        @Override
        public void onStart(int what) {
            startAnimateLoading();
            dialogPlus.show();
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
            dialogPlus.dismiss();
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
