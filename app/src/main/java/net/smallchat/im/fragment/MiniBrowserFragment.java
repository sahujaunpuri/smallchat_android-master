package net.smallchat.im.fragment;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.smallchat.im.chat.ChatMainActivity;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.MiniBrowserActivity;
import net.smallchat.im.R;
import net.smallchat.im.dialog.MMAlert;
import net.smallchat.im.global.GlobalParam;

import java.net.URISyntaxException;

/**
 * A simple {@link Fragment} subclass.
 */
public class MiniBrowserFragment extends Fragment {
    public final static int FILE_SELECTED = 1;

    private WebView mContentView;

    public final static String EXTRA_IS_ROOT = "is_root";
    public final static String EXTRA_URL = "url";

    private boolean isRoot;
    private String mToken;

    private boolean canGoBack = true;

    private String mImageFilePath;

    private String mBackUrl;

    public MiniBrowserFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mini_browser, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        String url = bundle.getString(EXTRA_URL);

        if (TextUtils.isEmpty(url)) {
            getActivity().finish();
            return;
        }
        isRoot = bundle.getBoolean(EXTRA_IS_ROOT, false);
        mToken="";
        //mToken = IMServerAPI.getToken(getActivity());

        mContentView = (WebView) view.findViewById(R.id.miniBrowserWebView);
        WebSettings settings = mContentView.getSettings();
        mContentView.addJavascriptInterface(new MiniBrowserJavaScriptObject(), "MiniBrowser");
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        mContentView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (isRoot) {
                    Intent intent = new Intent(getActivity(), MiniBrowserActivity.class);
                    intent.putExtra(EXTRA_URL, url);
                    startActivity(intent);
                    return true;
                }

                if (url.startsWith("mailto:") || url.startsWith("geo:") || url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }

                if (!url.startsWith("http")) {
                    return startActivityForUrl(url);
                }

                return false;
            }
        });

        mContentView.setWebChromeClient(new MyWebClient());
        mContentView.loadUrl(addToken(url));
    }


    public void goBack() {

        if (!TextUtils.isEmpty(mBackUrl)) {
            mContentView.loadUrl(addToken(mBackUrl));
            mBackUrl = null;
        } else if (canGoBack && mContentView.canGoBack()) {
            mContentView.goBack();
        } else {
            getActivity().finish();
        }
    }

    private String addToken(String url) {

        if (url.contains("?")) {
            return url + "&token=" + mToken;
        } else {
            return url + "?token=" + mToken;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case FILE_SELECTED: {
//                if (android.os.Build.VERSION.SDK_INT > 20) {
//                    if (mUploadHandlerL == null)
//                        return;
//                   // mUploadHandlerL.onResult(resultCode, data);
//
//                } else {
//                    if (mUploadHandler == null)
//                        return;
//                   // mUploadHandler.onResult(resultCode, data);
//                }
            }

            case GlobalParam.REQUEST_GET_URI:
                if (resultCode == Activity.RESULT_OK) {
                    //Gallery.doChoose(true, data, true, this);
                }
                break;

            case GlobalParam.REQUEST_GET_IMAGE_BY_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    //Gallery.doChoose(false, data, true, this);
                }
                break;
            case GlobalParam.REQUEST_GET_BITMAP:
                if (resultCode == Activity.RESULT_OK) {
                   // mImageFilePath = Gallery.getCropTempFileString();
                    //uploadImage(mImageFilePath);
                }
                break;
        }
    }

    private boolean startActivityForXmeye() {
        String pkgName = "com.mobile.myeye";
        Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage(pkgName);
        if (intent != null) {
            startActivity(intent);
            return true;
        }

        return false;
    }

    private boolean startActivityForUrl(String url) {
        Intent intent;
        // perform generic parsing of the URI to turn it into an Intent.
        try {
            intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
        } catch (URISyntaxException ex) {
            Log.w("Browser", "Bad URI " + url + ": " + ex.getMessage());
            return false;
        }
        // check whether the intent can be resolved. If not, we will see
        // whether we can download it from the Market.
        ResolveInfo r = null;
        try {
            r = getActivity().getPackageManager().resolveActivity(intent, 0);
        } catch (Exception e) {
            return false;
        }

        if (r == null) {
            return false;
        }

        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setComponent(null);

        try {
            if (getActivity().startActivityIfNeeded(intent, -1)) {
                return true;
            }
        } catch (ActivityNotFoundException ex) {
            // ignore the error. If no application can handle the URL,
            // eg about:blank, assume the browser can handle it.
        }
        return false;
    }


    public class MyWebClient extends WebChromeClient {

        // Android < 3.0 调用这个方法
        public void openFileChooser(ValueCallback<Uri> uploadFile) {

        }

        // 3.0 + 调用这个方法
        public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType) {

        }


        // Android > 4.1.1 调用这个方法
        public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {

        }

        //Android > 5.0 调用这个方法
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                         FileChooserParams fileChooserParams) {

            return true;
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, true);
        }

        @Override
        public void onGeolocationPermissionsHidePrompt() {
            super.onGeolocationPermissionsHidePrompt();
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);

            Activity activity = getActivity();
            if (activity instanceof MiniBrowserActivity) {
                ((MiniBrowserActivity) activity).setMiniBrowserTitle(title);
            }
        }
    }

    /*
     * 选择图片对话框
     */
    private void selectImg() {
        MMAlert.showAlert(getContext(), "", getActivity().getResources().
                        getStringArray(R.array.camer_item),
                null, new MMAlert.OnAlertSelectId() {

                    @Override
                    public void onClick(int whichButton) {
                        switch (whichButton) {
                            case 0:
                                //Gallery.getImageFromGallery(MiniBrowserFragment.this);
                                break;
                            case 1:
                                //Gallery.getImageFromCamera(MiniBrowserFragment.this);
                                break;
                            default:
                                break;
                        }
                    }
                });
    }

    public class MiniBrowserJavaScriptObject {

        @JavascriptInterface
        public void sendMessage(String uid, String name) {
            Login user = new Login();
            user.uid = uid;
            user.nickname = name;
            user.mIsRoom = 100;
            Intent intent = new Intent(getContext(), ChatMainActivity.class);
            intent.putExtra("data", user);
            startActivity(intent);
        }

        @JavascriptInterface
        public void setCanGoBack(boolean goBack) {
            canGoBack = goBack;
        }

        @JavascriptInterface
        public void selectImage() {
            selectImg();
        }

        @JavascriptInterface
        public void setBackUrl(String url) {
            mBackUrl = url;
        }
    }
}
