package us.zoom.sdksample.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import us.zoom.sdk.InMeetingNotificationHandle;
import us.zoom.sdk.JoinMeetingOptions;
import us.zoom.sdk.JoinMeetingParams;
import us.zoom.sdk.MeetingParameter;
import us.zoom.sdk.MeetingServiceListener;
import us.zoom.sdk.MeetingStatus;
import us.zoom.sdk.SimpleZoomUIDelegate;
import us.zoom.sdk.ZoomApiError;
import us.zoom.sdk.ZoomAuthenticationError;
import us.zoom.sdk.ZoomError;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdksample.R;
import us.zoom.sdksample.initsdk.InitAuthSDKCallback;
import us.zoom.sdksample.initsdk.InitAuthSDKHelper;
import us.zoom.sdksample.initsdk.jwt.JwtFetcher;
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.MyMeetingActivity;
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.RawDataMeetingActivity;
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.MeetingWindowHelper;
import us.zoom.sdksample.inmeetingfunction.zoommeetingui.CustomNewZoomUIActivity;
import us.zoom.sdksample.inmeetingfunction.zoommeetingui.ZoomMeetingUISettingHelper;
import us.zoom.sdksample.startjoinmeeting.UserLoginCallback;
import us.zoom.sdksample.util.Constants;

public class InitAuthSDKActivity extends Activity implements InitAuthSDKCallback,
        MeetingServiceListener, UserLoginCallback.ZoomDemoAuthenticationListener,
        OnClickListener, CompoundButton.OnCheckedChangeListener, JwtFetcher.Callback {

    private final static String TAG = "DigiOS_Zoom";

    public static boolean showMeetingTokenUI = false;

    private View layoutJoin;
    private View mProgressPanel;
    private EditText numberEdit;
    private EditText nameEdit;
    private EditText meetingTokenEdit;
    private ZoomSDK mZoomSDK;

    private Button mReturnMeeting;

    private CheckBox mRememberMe;

    private boolean isResumed = false;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mZoomSDK = ZoomSDK.getInstance();
        if (mZoomSDK.isLoggedIn()) {
            finish();
            showEmailLoginUserStartJoinActivity();
            return;
        }

        setContentView(R.layout.init_auth_sdk);
        prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);

        mProgressPanel = (View) findViewById(R.id.progressPanel);

        mReturnMeeting = findViewById(R.id.btn_return);
        layoutJoin = findViewById(R.id.layout_join);

        mRememberMe = findViewById(R.id.check_remember);
        mRememberMe.setChecked(isUserRemembered());
        mRememberMe.setOnCheckedChangeListener(this);

        numberEdit = findViewById(R.id.edit_join_number);
        numberEdit.setText(getSavedMeetingId());

        nameEdit = findViewById(R.id.edit_join_name);
        nameEdit.setText(getSavedUsername());

        meetingTokenEdit = findViewById(R.id.edit_join_meeting_token);
        mProgressPanel.setVisibility(View.GONE);
        if (showMeetingTokenUI) {
            meetingTokenEdit.setVisibility(View.VISIBLE);
        }

        JwtFetcher.Companion.newInstance(this).execute();
    }

    InMeetingNotificationHandle handle = (context, intent) -> {
        intent = new Intent(context, MyMeetingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.setAction(InMeetingNotificationHandle.ACTION_RETURN_TO_CONF);
        context.startActivity(intent);
        return true;
    };

    @Override
    public void onPreExecution() {
        layoutJoin.setVisibility(View.GONE);
        showSettings(false);
    }

    @Override
    public void onPostExecution(@NonNull String signature) {
        if (signature.isEmpty()) {
            Toast.makeText(context(), "Empty JWT", Toast.LENGTH_SHORT).show();
            return;
        }
        InitAuthSDKHelper.SDK_JWT = signature;
        InitAuthSDKHelper.getInstance().initSDK(this, this);

        if (mZoomSDK.isInitialized()) {
            layoutJoin.setVisibility(View.VISIBLE);
            layoutJoin.requestFocus();
            showSettings(true);

            mZoomSDK.getMeetingService().addListener(this);
            mZoomSDK.getMeetingSettingsHelper().enable720p(false);
        } else {
            layoutJoin.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    public Context context() {
        return this;
    }

    @Override
    public void onBackPressed() {
        if (null == mZoomSDK.getMeetingService()) {
            super.onBackPressed();
            return;
        }
        MeetingStatus meetingStatus = mZoomSDK.getMeetingService().getMeetingStatus();
        if (meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING) {
            moveTaskToBack(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onZoomSDKInitializeResult(int errorCode, int internalErrorCode) {
        Log.i(TAG, "onZoomSDKInitializeResult, errorCode=" + errorCode + ", internalErrorCode=" + internalErrorCode);

        if (errorCode != ZoomError.ZOOM_ERROR_SUCCESS) {
            Toast.makeText(this, "Failed to initialize Zoom SDK. Error: " + errorCode + ", internalErrorCode=" + internalErrorCode, Toast.LENGTH_LONG).show();
        } else {
//            mZoomSDK.getZoomUIService().enableMinimizeMeeting(true);
//            mZoomSDK.getZoomUIService().setMiniMeetingViewSize(new CustomizedMiniMeetingViewSize(0, 0, 360, 540));
//            setMiniWindows();
            mZoomSDK.getZoomUIService().setNewMeetingUI(CustomNewZoomUIActivity.class);
            mZoomSDK.getZoomUIService().disablePIPMode(false);
            mZoomSDK.getMeetingSettingsHelper().enable720p(false);
            mZoomSDK.getMeetingSettingsHelper().enableShowMyMeetingElapseTime(true);
            mZoomSDK.getMeetingService().addListener(this);
            mZoomSDK.getMeetingSettingsHelper().setCustomizedNotificationData(null, handle);
            Toast.makeText(this, "Initialize Zoom SDK successfully.", Toast.LENGTH_LONG).show();

            if (mZoomSDK.tryAutoLoginZoom() == ZoomApiError.ZOOM_API_ERROR_SUCCESS) {
                UserLoginCallback.getInstance().addListener(this);
                showProgressPanel(true);
            } else {
                showProgressPanel(false);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (!mZoomSDK.isInitialized()) {
            Toast.makeText(this, "Init SDK First", Toast.LENGTH_SHORT).show();
            InitAuthSDKHelper.getInstance().initSDK(this, this);
        }
//        if (v.getId() == R.id.btnSSOLogin) {
//            showSSOLoginActivity();
//        } else if (v.getId() == R.id.btnWithoutLogin) {
//            showAPIUserActivity();
//        }
    }

    public void onClickSettings(View view) {
        if (!mZoomSDK.isInitialized()) {
            Toast.makeText(this, "Init SDK First", Toast.LENGTH_SHORT).show();
            InitAuthSDKHelper.getInstance().initSDK(this, this);
            return;
        }
        startActivity(new Intent(this, MeetingSettingActivity.class));
    }

    @Override
    public void onZoomSDKLoginResult(long result) {
        if ((int) result == ZoomAuthenticationError.ZOOM_AUTH_ERROR_SUCCESS) {
            showEmailLoginUserStartJoinActivity();
            finish();
        } else {
            showProgressPanel(false);
        }
    }

    @Override
    public void onZoomSDKLogoutResult(long result) {

    }

    @Override
    public void onZoomIdentityExpired() {
        Log.e(TAG, "onZoomIdentityExpired");
        if (mZoomSDK.isLoggedIn()) {
            mZoomSDK.logoutZoom();
        }
    }

    @Override
    public void onZoomAuthIdentityExpired() {
        Log.d(TAG, "onZoomAuthIdentityExpired");
        JwtFetcher.Companion.newInstance(this).execute();
    }

    public void onClickJoin(View view) {
        if (!mZoomSDK.isInitialized()) {
            Toast.makeText(this, "Init SDK First", Toast.LENGTH_SHORT).show();
            InitAuthSDKHelper.getInstance().initSDK(this, this);
            return;
        }

        mZoomSDK.getSmsService().enableZoomAuthRealNameMeetingUIShown(
                !mZoomSDK.getMeetingSettingsHelper().isCustomizedMeetingUIEnabled()
        );
        String number = numberEdit.getText().toString();
        String name = nameEdit.getText().toString();
        String zoomMeetingToken = meetingTokenEdit.getText().toString();

        JoinMeetingParams params = new JoinMeetingParams();
        params.meetingNo = number;
        params.displayName = name;
        params.join_token = zoomMeetingToken;
        JoinMeetingOptions options = new JoinMeetingOptions();
        mZoomSDK.getMeetingService().joinMeetingWithParams(
                this, params, ZoomMeetingUISettingHelper.getJoinMeetingOptions()
        );

        saveUserInput();
    }

    private void showProgressPanel(boolean show) {
        if (show) {
            mReturnMeeting.setVisibility(View.GONE);
            mProgressPanel.setVisibility(View.VISIBLE);
            layoutJoin.setVisibility(View.GONE);
            showSettings(false);
        } else {
            showSettings(true);
            mProgressPanel.setVisibility(View.GONE);
            layoutJoin.setVisibility(View.VISIBLE);
            mReturnMeeting.setVisibility(View.GONE);
        }
    }

    private void showSSOLoginActivity() {
        Intent intent = new Intent(this, SSOUserLoginActivity.class);
        startActivity(intent);
    }

    private void showAPIUserActivity() {
        Intent intent = new Intent(this, APIUserStartJoinMeetingActivity.class);
        startActivity(intent);
    }

    private void showEmailLoginUserStartJoinActivity() {
        Intent intent = new Intent(this, LoginUserStartJoinMeetingActivity.class);
        startActivity(intent);
    }

    public void onClickReturnMeeting(View view) {
        UIUtil.returnToMeeting(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
        refreshUI();

        setMiniWindows();
    }

    private void setMiniWindows() {
        if (null != mZoomSDK && mZoomSDK.isInitialized() && !mZoomSDK.getMeetingSettingsHelper().isCustomizedMeetingUIEnabled()) {
            mZoomSDK.getZoomUIService().setZoomUIDelegate(new SimpleZoomUIDelegate() {
                @Override
                public void afterMeetingMinimized(Activity activity) {
                    Intent intent = new Intent(activity, InitAuthSDKActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    activity.startActivity(intent);
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isResumed = false;
    }

    private void refreshUI() {
        if (!mZoomSDK.isInitialized()) {
            return;
        }
        MeetingStatus meetingStatus = mZoomSDK.getMeetingService().getMeetingStatus();
        if (mZoomSDK.getMeetingSettingsHelper().isCustomizedMeetingUIEnabled()) {
            if (meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING && isResumed) {
                MeetingWindowHelper.getInstance().showMeetingWindow(this);
                showProgressPanel(true);
                mProgressPanel.setVisibility(View.GONE);
                mReturnMeeting.setVisibility(View.VISIBLE);
            } else {
                MeetingWindowHelper.getInstance().hiddenMeetingWindow(true);
                showProgressPanel(false);
            }
        } else {
            if (meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING && isResumed) {
                showProgressPanel(true);
                mProgressPanel.setVisibility(View.GONE);
                mReturnMeeting.setVisibility(View.VISIBLE);
            } else {
                showProgressPanel(false);
            }
        }
    }


    @Override
    public void onMeetingParameterNotification(MeetingParameter meetingParameter) {
        Log.d(TAG, "onMeetingParameterNotification " + meetingParameter);
    }

    @Override
    public void onMeetingStatusChanged(MeetingStatus meetingStatus, int errorCode, int internalErrorCode) {
        Log.d(TAG, "onMeetingStatusChanged " + meetingStatus + ":" + errorCode + ":" + internalErrorCode);
        if (!mZoomSDK.isInitialized()) {
            showProgressPanel(false);
            return;
        }
        if (meetingStatus == MeetingStatus.MEETING_STATUS_CONNECTING) {
            if (ZoomMeetingUISettingHelper.useExternalVideoSource) {
                ZoomMeetingUISettingHelper.changeVideoSource(true, InitAuthSDKActivity.this);
            }
        }
        if (mZoomSDK.getMeetingSettingsHelper().isCustomizedMeetingUIEnabled()) {
            if (meetingStatus == MeetingStatus.MEETING_STATUS_CONNECTING) {
                showMeetingUi();
            }
        }
        refreshUI();
    }

    private void showMeetingUi() {
        if (mZoomSDK.getMeetingSettingsHelper().isCustomizedMeetingUIEnabled()) {
            SharedPreferences sharedPreferences = getSharedPreferences("UI_Setting", Context.MODE_PRIVATE);
            boolean enable = sharedPreferences.getBoolean("enable_rawdata", false);
            Intent intent = null;
            if (!enable) {
                intent = new Intent(this, MyMeetingActivity.class);
                intent.putExtra("from", MyMeetingActivity.JOIN_FROM_UNLOGIN);
            } else {
                intent = new Intent(this, RawDataMeetingActivity.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            this.startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MeetingWindowHelper.getInstance().onActivityResult(requestCode, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UserLoginCallback.getInstance().removeListener(this);

        if (null != mZoomSDK.getMeetingService()) {
            mZoomSDK.getMeetingService().removeListener(this);
        }
        if (null != mZoomSDK.getMeetingSettingsHelper()) {
            mZoomSDK.getMeetingSettingsHelper().setCustomizedNotificationData(null, null);
        }
        InitAuthSDKHelper.getInstance().reset();
    }

    private void showSettings(boolean show) {
        int visibility = Constants.INSTANCE.getBoolean(Constants.PROPERTY_SHOW_SETTINGS) ?
                show ? View.VISIBLE : View.GONE : View.GONE;
        View view = findViewById(R.id.btnSettings);
        if (null != view) {
            view.setVisibility(visibility);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.PREFS_IS_REMEMBERED, isChecked);
        if (!isChecked) {
            editor.putString(Constants.PREFS_MEETING_ID, "");
            editor.putString(Constants.PREFS_USERNAME, "");
        }
        editor.apply();
    }

    private boolean isUserRemembered() {
        return prefs.getBoolean(Constants.PREFS_IS_REMEMBERED, false);
    }

    private String getSavedMeetingId() {
        return prefs.getString(Constants.PREFS_MEETING_ID, "");
    }

    private String getSavedUsername() {
        return prefs.getString(Constants.PREFS_USERNAME, "");
    }

    private void saveUserInput() {
        SharedPreferences.Editor editor = prefs.edit();
        String meetingId = "";
        String username = "";
        if (mRememberMe.isChecked()) {
            meetingId = numberEdit.getText().toString();
            username = nameEdit.getText().toString();
        }
        editor.putString(Constants.PREFS_MEETING_ID, meetingId);
        editor.putString(Constants.PREFS_USERNAME, username);
        editor.apply();
    }
}