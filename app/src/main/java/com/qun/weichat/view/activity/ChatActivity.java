package com.qun.weichat.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.chat.EMMessage;
import com.qun.weichat.R;
import com.qun.weichat.adapter.ChatAdapter;
import com.qun.weichat.presenter.ChatPresenter;
import com.qun.weichat.presenter.ChatPresenterImpl;
import com.qun.weichat.utils.ToastUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatActivity extends AppCompatActivity implements TextWatcher, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, ChatView {

    private static final int REQUEST_PIC = 100;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.toolBar)
    Toolbar mToolBar;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.et_msg)
    EditText mEtMsg;
    @BindView(R.id.btn_send)
    Button mBtnSend;
    @BindView(R.id.iv_pic)
    ImageView mIvPic;
    @BindView(R.id.iv_camera)
    ImageView mIvCamera;
    private String mUsername;
    private ChatPresenter mChatPresenter;
    //每页多少条聊天记录
    private int pageSize = 20;
    private ChatAdapter mChatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        initView();
        initData();
    }

    private void initView() {
        mToolBar.setTitle("");
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mUsername = getIntent().getStringExtra("username");
        if (TextUtils.isEmpty(mUsername)) {
            ToastUtil.showMsg(this, "聊天对象为空");
            finish();
            return;
        } else {
            mTvTitle.setText("与" + mUsername + "聊天中");
        }
        mEtMsg.addTextChangedListener(this);
        checkEditText();
        mIvPic.setOnClickListener(this);
        mIvCamera.setOnClickListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorAccent));
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    //获取历史聊天记录，然后展示到RecyclerView上
    private void initData() {
        mChatPresenter = new ChatPresenterImpl(this);
        mChatPresenter.init(mUsername, pageSize);
    }

    private void checkEditText() {
        String msg = mEtMsg.getText().toString();
        if (TextUtils.isEmpty(msg)) {
            mBtnSend.setEnabled(false);
        } else {
            mBtnSend.setEnabled(true);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    /**
     * @param s mEtMsg.getText()
     */
    @Override
    public void afterTextChanged(Editable s) {
        checkEditText();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_pic:
                choosePicture();
                break;
            case R.id.iv_camera:

                break;
            case R.id.btn_send:
                sendMsg();
                break;
        }
    }

    /**
     * 从系统图库中选择图片
     */
    private void choosePicture() {
        /**
         * Action
         * data MediaStore.Images.Media.EXTERNAL_CONTENT_URI
         */
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PIC);
    }

    private void sendMsg() {

    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onInit(List<EMMessage> emMessageList) {
        mChatAdapter = new ChatAdapter(emMessageList,this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mChatAdapter);
        mRecyclerView.scrollToPosition(emMessageList.size() - 1);
    }
}
