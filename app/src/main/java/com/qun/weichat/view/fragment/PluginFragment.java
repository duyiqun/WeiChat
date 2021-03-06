package com.qun.weichat.view.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.hyphenate.chat.EMClient;
import com.qun.weichat.MainActivity;
import com.qun.weichat.R;
import com.qun.weichat.presenter.PluginPresenter;
import com.qun.weichat.presenter.PluginPresenterImpl;
import com.qun.weichat.utils.ToastUtil;
import com.qun.weichat.view.activity.LoginActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class PluginFragment extends BaseFragment implements PluginView, View.OnClickListener {

    private PluginPresenter mPluginPresenter;
    private Button mBtnLogout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_plugin, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBtnLogout = (Button) view.findViewById(R.id.btn_logout);
        mBtnLogout.setOnClickListener(this);
        //获取当前的用户名
        String currentUser = EMClient.getInstance().getCurrentUser();
        mBtnLogout.setText("退(" + currentUser + ")出");
        mPluginPresenter = new PluginPresenterImpl(this);
    }

    @Override
    public void onClick(View v) {
        mPluginPresenter.logout();
    }

    @Override
    public void onLogout(boolean isSuccess, String msg) {
        if (!isSuccess) {
            ToastUtil.showMsg(getContext(), msg);
        }
        MainActivity activity = (MainActivity) getActivity();
        activity.startActivity(LoginActivity.class, true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBtnLogout = null;
    }
}
