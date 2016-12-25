package cn.mcavoy.www.subwayticket;

import android.content.Intent;
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

import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.error.TimeoutError;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;
import com.yolanda.nohttp.rest.StringRequest;

import cn.mcavoy.www.subwayticket.Application.MetroApplication;

public class RegisterActivity extends AppCompatActivity {
    private TextView turnToSignIn;
    private Button btn_signUp;
    private MaterialEditText emailEditText, passwordEditText, nameEditText, checkPassEidtText, mobileEditText;

    private DialogPlus dialogPlus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        Window window = this.getWindow();
        window.setFlags(flag, flag);
        setContentView(R.layout.register_main);
        turnToSignIn = (TextView) findViewById(R.id.turnTo_signIn);
        turnToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InterfaceToLogin();
            }
        });

        emailEditText = (MaterialEditText) findViewById(R.id.register_textField_email);
        passwordEditText = (MaterialEditText) findViewById(R.id.register_textField_pass);
        nameEditText = (MaterialEditText) findViewById(R.id.register_textField_name);
        checkPassEidtText = (MaterialEditText) findViewById(R.id.register_textField_checkpass);
        mobileEditText = (MaterialEditText) findViewById(R.id.register_textField_mobile);

        emailEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);
        nameEditText.addTextChangedListener(textWatcher);
        checkPassEidtText.addTextChangedListener(textWatcher);
        mobileEditText.addTextChangedListener(textWatcher);

        btn_signUp = (Button) findViewById(R.id.btn_signUp);
        btn_signUp.setEnabled(false);
        btn_signUp.setOnClickListener(btnListener);
    }

    View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Request<String> registerRequest = new StringRequest(MetroApplication.registerApi, RequestMethod.POST);
            registerRequest.add("email", emailEditText.getText().toString());
            registerRequest.add("password", passwordEditText.getText().toString());
            registerRequest.add("name", nameEditText.getText().toString());
            registerRequest.add("mobile", mobileEditText.getText().toString());
            CallServer.getInstance().add(0, registerRequest, listener);
        }
    };

    //跳转到登录的函数
    public void InterfaceToLogin() {
        Intent intent = new Intent();
        intent.setClass(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    //加载动画初始化
    public void startAnimateLoading() {
        dialogPlus = DialogPlus.newDialog(RegisterActivity.this)
                .setContentHolder(new ViewHolder(R.layout.dialog_loading))
                .setContentBackgroundResource(Color.TRANSPARENT)
                .setGravity(Gravity.CENTER)
                .setCancelable(false)
                .create();
    }

    OnResponseListener listener = new OnResponseListener() {
        @Override
        public void onStart(int what) {
            startAnimateLoading();
            dialogPlus.show();
        }

        @Override
        public void onSucceed(int what, Response response) {
            if (what == 0) {
                if (response.responseCode() == 200) {
                    Toast.makeText(getBaseContext(), "注册成功!", Toast.LENGTH_SHORT).show();
                    InterfaceToLogin();
                } else {
                    Toast.makeText(getBaseContext(), "注册成功!请重新注册!", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onFailed(int what, Response response) {
            if (what == 0) {
                Exception exception = response.getException();
                if (exception instanceof TimeoutError) {
                    Toast.makeText(getBaseContext(), "连接服务器失败!", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onFinish(int what) {
            dialogPlus.dismiss();
        }
    };

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (emailEditText.length() != 0
                    && passwordEditText.length() != 0
                    && nameEditText.length() != 0
                    && checkPassEidtText.length() != 0
                    && mobileEditText.length() != 0) {
                if (passwordEditText.getText().toString().equals(checkPassEidtText.getText().toString())) {
                    btn_signUp.setEnabled(true);
                } else {
                    btn_signUp.setEnabled(false);
                }
            } else {
                btn_signUp.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
}
