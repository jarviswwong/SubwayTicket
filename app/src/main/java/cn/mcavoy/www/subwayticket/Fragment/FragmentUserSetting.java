package cn.mcavoy.www.subwayticket.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import cn.mcavoy.www.subwayticket.CallServer;
import cn.mcavoy.www.subwayticket.R;


public class FragmentUserSetting extends Fragment {
    private MaterialEditText mobile, name, introduction, userpass, checkuserpass;
    private Button submit;
    private View view;

    private DialogPlus dialogPlus;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.user_main, container, false);
        setHasOptionsMenu(true);
        mobile = (MaterialEditText) view.findViewById(R.id.edit_user_mobile);
        name = (MaterialEditText) view.findViewById(R.id.edit_user_name);
        introduction = (MaterialEditText) view.findViewById(R.id.edit_user_introduction);
        userpass = (MaterialEditText) view.findViewById(R.id.edit_user_pass);
        checkuserpass = (MaterialEditText) view.findViewById(R.id.edit_user_checkPass);
        mobile.addTextChangedListener(textwatcher);
        name.addTextChangedListener(textwatcher);
        introduction.addTextChangedListener(textwatcher);
        userpass.addTextChangedListener(textwatcher);
        checkuserpass.addTextChangedListener(textwatcher);

        submit = (Button) view.findViewById(R.id.btn_userEdit);
        submit.setEnabled(false);
        submit.setOnClickListener(buttonListener);
        return view;
    }

    View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("111", "click submit");
            Request<String> editRequest = new StringRequest(MetroApplication.editUserApi, RequestMethod.POST);
            SharedPreferences sp = getActivity().getSharedPreferences("user_validate", Context.MODE_PRIVATE);
            //假如存在
            if (sp.contains("user_token")) {
                editRequest.addHeader("Authorization", "Bearer " + sp.getString("user_token", null));
                editRequest.addHeader("userId", MetroApplication.userModel.getId());
                if (mobile.getText().toString().length() > 0) {
                    editRequest.add("mobile", mobile.getText().toString());
                }
                if (name.getText().toString().length() > 0) {
                    editRequest.add("name", name.getText().toString());
                }
                if (introduction.getText().toString().length() > 0) {
                    editRequest.add("introduction", introduction.getText().toString());
                }
                if (userpass.getText().length() > 0 && checkuserpass.getText().length() > 0) {
                    if (userpass.getText().toString().equals(checkuserpass.getText().toString())) {
                        editRequest.add("userpass", userpass.getText().toString());
                        CallServer.getInstance().add(0, editRequest, listener);
                    } else {
                        Toast.makeText(view.getContext(), "新密码与确认新密码必须一致!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    CallServer.getInstance().add(0, editRequest, listener);
                }
            } else {
                Toast.makeText(view.getContext(), "验证用户失败，请重新登录!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    //监测输入的变化
    TextWatcher textwatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mobile.getText().toString().length() > 0 || name.getText().toString().length() > 0
                    | userpass.getText().toString().length() > 0 || introduction.getText().toString().length() > 0) {
                submit.setEnabled(true);
            } else {
                submit.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void startAnimateEdit() {
        dialogPlus = DialogPlus.newDialog(view.getContext())
                .setContentHolder(new ViewHolder(R.layout.dialog_loading_edituser))
                .setContentBackgroundResource(Color.TRANSPARENT)
                .setGravity(Gravity.CENTER)
                .setCancelable(false)
                .create();
    }

    //提交修改http监听
    OnResponseListener listener = new OnResponseListener() {
        @Override
        public void onStart(int what) {
            startAnimateEdit();
            dialogPlus.show();
        }

        @Override
        public void onSucceed(int what, Response response) {
            if (what == 0) {
                if (response.responseCode() == 200) {
                    Toast.makeText(view.getContext(), "修改个人信息成功！", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(view.getContext(), "修改个人信息失败！", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onFailed(int what, Response response) {
            if (what == 0) {
                Exception exception = response.getException();
                if (exception instanceof TimeoutError) {
                    Toast.makeText(view.getContext(), "服务器连接超时！", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onFinish(int what) {
            dialogPlus.dismiss();
            mobile.setText("");
            name.setText("");
            introduction.setText("");
            userpass.setText("");
            checkuserpass.setText("");
        }
    };
}
