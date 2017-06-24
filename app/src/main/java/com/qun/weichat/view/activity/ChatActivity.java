package com.qun.weichat.view.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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

import com.bumptech.glide.Glide;
import com.hyphenate.chat.EMMessage;
import com.qun.weichat.R;
import com.qun.weichat.adapter.ChatAdapter;
import com.qun.weichat.presenter.ChatPresenter;
import com.qun.weichat.presenter.ChatPresenterImpl;
import com.qun.weichat.utils.ToastUtil;
import com.qun.weichat.widget.KeyboardListenerLinearLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatActivity extends AppCompatActivity implements TextWatcher, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, ChatView, KeyboardListenerLinearLayout.OnKeyboardChangedListener {

    private static final int REQUEST_PIC = 100;
    private static final String TAG = "ChatActivity";
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
    private KeyboardListenerLinearLayout mKeyboardListenerLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        initView();
        initData();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EMMessage emMessage) {
        //判断这个消息是否是当前的聊天对象给我发送的
        if (emMessage.getFrom().equals(mUsername)) {
            //将emMessage发送给ChatPresenter ,让P层将emMessage添加到集合中
            mChatPresenter.receiveMsg(emMessage);
            mChatAdapter.notifyDataSetChanged();
            mRecyclerView.smoothScrollToPosition(mChatAdapter.getItemCount() - 1);
        }
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
        mBtnSend.setOnClickListener(this);

//        LinearLayout llChat = (LinearLayout) findViewById(R.id.ll_chat);
//        llChat.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
//            @Override
//            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                ToastUtil.showMsg(getApplicationContext(), "改变后的：" + bottom + "/改变前的：" + oldBottom);
//            }
//        });

        mKeyboardListenerLinearLayout = (KeyboardListenerLinearLayout) findViewById(R.id.ll_chat);
        mKeyboardListenerLinearLayout.setOnKeyboardChangedListener(this);
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
            default:
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PIC) {
            if (resultCode == RESULT_OK) {
//                ToastUtil.showMsg(this, "选择图片：" + data.getData());
//                Log.d(TAG, "onActivityResult：" + data.getData());

                Uri uri = data.getData();
                Cursor cursor = getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
                if (cursor.moveToFirst()) {
                    String imagePath = cursor.getString(0);
                    sendImageMsg(imagePath);
                }
                cursor.close();
            } else {
                ToastUtil.showMsg(this, "没有选择图片");
            }
        }
    }

    private void sendImageMsg(String imagePath) {
        mChatPresenter.sendImageMsg(imagePath, mUsername);
    }

    private void sendMsg() {
        String msg = mEtMsg.getText().toString();
        if (TextUtils.isEmpty(msg)) {
            ToastUtil.showMsg(this, "发送的内容不能为空");
            return;
        }
        mEtMsg.getText().clear();
        mChatPresenter.sendTextMessage(msg, mUsername);
    }

    @Override
    public void onRefresh() {
        //加载更多的聊天记录
        mChatPresenter.loadMoreMsg(pageSize);
    }

    @Override
    public void onInit(List<EMMessage> emMessageList) {
        mChatAdapter = new ChatAdapter(emMessageList, this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mChatAdapter);
        mRecyclerView.scrollToPosition(emMessageList.size() - 1);

        //监听RecyclerView的滚动事件
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE://停止
                        Glide.with(ChatActivity.this.getApplicationContext()).resumeRequests();
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING://手动拖拽滚动
                    case RecyclerView.SCROLL_STATE_SETTLING://惯性滚动
                        Glide.with(ChatActivity.this.getApplicationContext()).pauseRequests();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    @Override
    public void onSendMsg(EMMessage message) {
        mChatAdapter.notifyDataSetChanged();
        //让RecyclerView平滑滚动一条
        mRecyclerView.smoothScrollToPosition(mChatAdapter.getItemCount() - 1);
    }

    @Override
    public void onLoadMore(boolean isSuccess, String msg, int loadCount) {
        mSwipeRefreshLayout.setRefreshing(false);
        if (isSuccess) {
            mChatAdapter.notifyDataSetChanged();
            mRecyclerView.scrollToPosition(loadCount);
        }
        ToastUtil.showMsg(this, msg);
    }

    @Override
    public void onKeyboardChanged(boolean isOpen) {
        if (isOpen) {
            if (mRecyclerView != null && mChatAdapter != null) {
                mRecyclerView.scrollToPosition(mChatAdapter.getItemCount() - 1);
            }

            //可解决软键盘弹出
            LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
            int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

            View lastVisibleView = layoutManager.findViewByPosition(lastVisibleItemPosition);
            if (lastVisibleView != null) {
                int measuredHeight = lastVisibleView.getMeasuredHeight();
                layoutManager.scrollToPositionWithOffset(lastVisibleItemPosition, -measuredHeight);
            }
        }
    }
}
