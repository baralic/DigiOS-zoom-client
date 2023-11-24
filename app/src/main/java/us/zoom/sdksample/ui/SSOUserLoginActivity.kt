package us.zoom.sdksample.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import us.zoom.sdk.ZoomAuthenticationError
import us.zoom.sdk.ZoomSDK
import us.zoom.sdksample.R
import us.zoom.sdksample.startjoinmeeting.UserLoginCallback
import us.zoom.sdksample.startjoinmeeting.UserLoginCallback.ZoomDemoAuthenticationListener

class SSOUserLoginActivity : Activity(), ZoomDemoAuthenticationListener, View.OnClickListener {
    private lateinit var mEdtSSOPrefix: EditText
    private lateinit var mBtnConfirm: Button
    private lateinit var mProgressPanel: View
    private lateinit var mWebView: WebView
    private lateinit var mWebViewLoadingLayout: LinearLayout
    private lateinit var mSSOUrl: String
    private lateinit var mLoginOperationLayout: LinearLayout
    private lateinit var mLoginInfoEnterLayout: LinearLayout
    private lateinit var mUrlGenerateTv: TextView
    private lateinit var mEdtLoginUrl: EditText
    private lateinit var mBtnLoginConfirm: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sso_login_activity)
        mEdtSSOPrefix = findViewById<View>(R.id.edtSSODomainPrefix) as EditText
        mBtnConfirm = findViewById<View>(R.id.btnConfirm) as Button
        mBtnConfirm.setOnClickListener(this)
        mProgressPanel = findViewById(R.id.progressPanel)
        mWebViewLoadingLayout = findViewById(R.id.webViewProgressPanel)
        mWebViewLoadingLayout.visibility = View.GONE
        mLoginOperationLayout = findViewById(R.id.operationLayout)
        mLoginOperationLayout.visibility = View.GONE
        findViewById<View>(R.id.btnLoginBySelf).setOnClickListener(this)
        findViewById<View>(R.id.btnAUtoLogin).setOnClickListener(this)
        initWebView()
        initManualLoginLayout()
        UserLoginCallback.getInstance().addListener(this)
    }

    private fun initWebView() {
        mWebView = findViewById(R.id.webview)
        mWebView.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (URLUtil.isNetworkUrl(url)) {
                    return false
                } else {
                    /* get the login url */
                    onLogin(url)
                }
                return true
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
                super.onPageStarted(view, url, favicon)
                mWebViewLoadingLayout.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                mWebViewLoadingLayout.visibility = View.GONE
            }
        })
        val webSettings = mWebView.getSettings()
        webSettings.javaScriptEnabled = true
    }

    private fun initManualLoginLayout() {
        mLoginInfoEnterLayout = findViewById(R.id.enterLoginInfoLayout)
        mLoginInfoEnterLayout.visibility = View.GONE
        mUrlGenerateTv = findViewById(R.id.tvGenerateUrl)
        mEdtLoginUrl = findViewById(R.id.edtLoginUrl)
        mBtnLoginConfirm = findViewById(R.id.btnLoginConfirm)
        mBtnLoginConfirm.setOnClickListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        UserLoginCallback.getInstance().removeListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnConfirm -> onClickBtnGenerateUrl()
            R.id.btnAUtoLogin -> onAutoLogin()
            R.id.btnLoginBySelf -> onLoginBySelf()
            R.id.btnLoginConfirm -> onLoginBySelfConfirm()
        }
    }

    private fun onLoginBySelfConfirm() {
        val ssoLoginUrl = mEdtLoginUrl.getText().toString().trim { it <= ' ' }
        if (ssoLoginUrl.isEmpty()) {
            Toast.makeText(this, "You need to enter sso token.", Toast.LENGTH_LONG).show()
            return
        }
        onLogin(ssoLoginUrl)
    }

    private fun onLogin(ssoLoginUrl: String) {
        mWebView.visibility = View.GONE
        mEdtSSOPrefix.visibility = View.GONE
        mBtnConfirm.visibility = View.GONE
        mLoginInfoEnterLayout.visibility = View.GONE
        mProgressPanel.visibility = View.VISIBLE
        if (!ZoomSDK.getInstance().handleSSOLoginURIProtocol(ssoLoginUrl)) {
            Toast.makeText(
                this,
                "ZoomSDK has not been initialized successfully or sdk is logging in.",
                Toast.LENGTH_LONG
            ).show()
            onLoginFinish()
        }
    }

    private fun onLoginFinish() {
        mProgressPanel.visibility = View.GONE
        mEdtSSOPrefix.visibility = View.VISIBLE
        mBtnConfirm.visibility = View.VISIBLE
    }

    private fun onClickBtnGenerateUrl() {
        val ssoDomainPrefix = mEdtSSOPrefix.getText().toString().trim { it <= ' ' }
        if (ssoDomainPrefix.isEmpty()) {
            Toast.makeText(this, "You need to enter sso token.", Toast.LENGTH_LONG).show()
            return
        }
        val ssoUrl = ZoomSDK.getInstance().generateSSOLoginURL(ssoDomainPrefix)
        if (null == ssoUrl || ssoUrl.isEmpty()) {
            Toast.makeText(this, "generate sso url error.", Toast.LENGTH_LONG).show()
            return
        }
        mSSOUrl = ssoUrl
        mEdtSSOPrefix.visibility = View.GONE
        mBtnConfirm.visibility = View.GONE
        mLoginOperationLayout.visibility = View.VISIBLE
    }

    private fun onAutoLogin() {
        mWebView.visibility = View.VISIBLE
        mWebView.loadUrl(mSSOUrl)
        /* web view Load may Blank page to avoid misunderstanding, add a loading state */mWebViewLoadingLayout.visibility =
            View.VISIBLE
        mLoginOperationLayout.visibility = View.GONE
        mLoginInfoEnterLayout.visibility = View.GONE
    }

    private fun onLoginBySelf() {
        mLoginOperationLayout.visibility = View.GONE
        mLoginInfoEnterLayout.visibility = View.VISIBLE
        mWebViewLoadingLayout.visibility = View.GONE
        mWebView.visibility = View.GONE
        mUrlGenerateTv.text = mSSOUrl
    }

    override fun onZoomSDKLoginResult(result: Long) {
        if (result == ZoomAuthenticationError.ZOOM_AUTH_ERROR_SUCCESS.toLong()) {
            Toast.makeText(this, "Login successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginUserStartJoinMeetingActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Login failed result code = $result", Toast.LENGTH_SHORT).show()
        }
        onLoginFinish()
    }

    override fun onZoomSDKLogoutResult(result: Long) {
        if (result == ZoomAuthenticationError.ZOOM_AUTH_ERROR_SUCCESS.toLong()) {
            Toast.makeText(this, "Logout successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Logout failed result code = $result", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onZoomIdentityExpired() {
        //Zoom identity expired, please re-login;
    }

    override fun onZoomAuthIdentityExpired() {}

    companion object {
        private const val TAG = "ZoomSDKExample"
    }
}
