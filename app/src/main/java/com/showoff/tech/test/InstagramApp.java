package com.showoff.tech.test;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class InstagramApp {

    private InstagramSession mSession;
    private InstagramDialog mDialog;
    private OAuthAuthenticationListener mListener;
    private ProgressDialog mProgress;
    private HashMap<String, String> userInfo = new HashMap<String, String>();
    private HashMap<String, String> userMediaDetail = new HashMap<String, String>();
    private ArrayList<String> userMediaList = new ArrayList<>();
    private String mAuthUrl;
    private String mTokenUrl;
    private String mAccessToken;
    private Context mCtx;
    private String mClientId;
    private String mClientSecret;
    public static int WHAT_FINALIZE = 0;
    public static int WHAT_MEDIA = 3;
    public static int WHAT_MEDIA_DETAILS = 4;
    public static int WHAT_ERROR = 1;
    private static int WHAT_FETCH_INFO = 2;

    public static String mCallbackUrl = "";
    private static final String AUTH_URL = "https://api.instagram.com/oauth/authorize/";
    private static final String TOKEN_URL = "https://api.instagram.com/oauth/access_token";
    private static final String API_URL = "https://graph.instagram.com";
    private static final String TAG = "InstagramAPI";
    public static final String TAG_DATA = "data";
    public static final String TAG_ID = "id";
    public static final String TAG_MEDIA_ID = "id";
    public static final String TAG_USERNAME = "username";
    public static final String TAG_MEDIA_TYPE = "media_type";
    public static final String TAG_MEDIA_URL = "media_url";
    public static final String TAG_TIMESTAMP= "timestamp";

    public InstagramApp(Context context, String clientId, String clientSecret, String callbackUrl) {

        mClientId = clientId;
        mClientSecret = clientSecret;
        mCtx = context;
        mSession = new InstagramSession(context);
        mAccessToken = mSession.getAccessToken();
        mCallbackUrl = callbackUrl;
        mTokenUrl = TOKEN_URL + "?client_id=" + clientId + "&client_secret="
                + clientSecret + "&redirect_uri=" + mCallbackUrl
                + "&grant_type=authorization_code";
        mAuthUrl = AUTH_URL
                + "?client_id="
                + clientId
                + "&redirect_uri="
                + mCallbackUrl
                + "&response_type=code&scope=user_profile+user_media";

        InstagramDialog.OAuthDialogListener listener = new InstagramDialog.OAuthDialogListener() {
            @Override
            public void onComplete(String code) {
                String updatedCode = removeLast(code, 2);
                getAccessToken(updatedCode);
            }

            @Override
            public void onError(String error) {
                mListener.onFail("Authorization failed");
            }
        };

        mDialog = new InstagramDialog(context, mAuthUrl, listener);
        mProgress = new ProgressDialog(context);
        mProgress.setCancelable(false);
    }

    public String removeLast(String s, int n) {
        if (null != s && !s.isEmpty()) {
            s = s.substring(0, s.length() - n);
        }
        return s;
    }

    private void getAccessToken(final String code) {
        mProgress.setMessage("Getting access token ...");
        mProgress.show();

        new Thread() {
            @Override
            public void run() {
                Log.i(TAG, "Getting access token");
                int what = WHAT_FETCH_INFO;
                try {
                    URL url = new URL(TOKEN_URL);
                    Log.i(TAG, "Opening Token URL " + url.toString());
                    HttpURLConnection urlConnection = (HttpURLConnection) url
                            .openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);
                    OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                    writer.write("client_id=" + mClientId + "&client_secret="
                            + mClientSecret + "&grant_type=authorization_code"
                            + "&redirect_uri=" + mCallbackUrl + "&code=" + code);
                    writer.flush();
                    String response = Utils.streamToString(urlConnection.getInputStream());
                    Log.i(TAG, "response " + response);
                    JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
                    mAccessToken = jsonObj.getString("access_token");
                    Log.i(TAG, "Got access token: " + mAccessToken);
                    String id = jsonObj.getString("user_id");
                    mSession.storeAccessToken(mAccessToken, id);
                } catch (Exception ex) {
                    what = WHAT_ERROR;
                    ex.printStackTrace();
                }

                mHandler.sendMessage(mHandler.obtainMessage(what, 1, 0));
            }
        }.start();
    }

    public void fetchUserName(final Handler handler) {
        mProgress = new ProgressDialog(mCtx);
        mProgress.setMessage("Loading ...");
        mProgress.show();

        new Thread() {
            @Override
            public void run() {
                Log.i(TAG, "Fetching user info");
                int what = WHAT_FINALIZE;
                try {
                    URL url = new URL(API_URL + "/" + mSession.getId()
                            + "?fields=id,username&access_token=" + mAccessToken);

                    Log.d(TAG, "Opening URL " + url.toString());
                    HttpURLConnection urlConnection = (HttpURLConnection) url
                            .openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoInput(true);
                    urlConnection.connect();
                    String response = Utils.streamToString(urlConnection.getInputStream());
                    System.out.println(response);
                    JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
                    userInfo.put(TAG_ID, jsonObj.getString(TAG_ID));
                    userInfo.put(TAG_USERNAME, jsonObj.getString(TAG_USERNAME));
                } catch (Exception ex) {
                    what = WHAT_ERROR;
                    ex.printStackTrace();
                }
                mProgress.dismiss();
                handler.sendMessage(handler.obtainMessage(what, 2, 0));
            }
        }.start();

    }

    public void fetchUserMedia(final Handler handler) {
        mProgress = new ProgressDialog(mCtx);
        mProgress.setMessage("Loading ...");
        mProgress.show();
        new Thread() {
            @Override
            public void run() {
                Log.i(TAG, "Fetching user media");
                int what = WHAT_MEDIA;
                try {

                    URL url = new URL(API_URL + "/" + mSession.getId() + "/media"
                            + "?fields=id,caption&access_token=" + mAccessToken);

                    Log.d(TAG, "Opening URL " + url.toString());
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoInput(true);
                    urlConnection.connect();
                    String response = Utils.streamToString(urlConnection.getInputStream());
                    System.out.println(response);
                    JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
                    JSONArray data_obj = jsonObj.getJSONArray(TAG_DATA);
                    if (data_obj != null && data_obj.length() != 0) {
                        for (int i = 0; i < data_obj.length(); i++) {
                            JSONObject id = (JSONObject) data_obj.get(i);
                            userMediaList.add(id.getString(TAG_ID));
                        }
                    }
                } catch (Exception ex) {
                    what = WHAT_ERROR;
                    ex.printStackTrace();
                }
                mProgress.dismiss();
                handler.sendMessage(handler.obtainMessage(what, 3, 0));
            }
        }.start();

    }

    public void fetchUserMediaDetails(final Handler handler, final String mediaID) {
        mProgress = new ProgressDialog(mCtx);
        mProgress.setMessage("Loading ...");
        mProgress.show();
        new Thread() {
            @Override
            public void run() {
                Log.i(TAG, "Fetching user media details");
                int what = WHAT_MEDIA_DETAILS;
                try {

                    URL url = new URL(API_URL + "/" + mediaID
                            + "?fields=id,media_type,media_url,username,timestamp&access_token=" + mAccessToken);

                    Log.d(TAG, "Opening URL " + url.toString());
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoInput(true);
                    urlConnection.connect();
                    String response = Utils.streamToString(urlConnection.getInputStream());
                    System.out.println(response);
                    JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
                    userMediaDetail.put(TAG_MEDIA_ID,jsonObj.getString(TAG_MEDIA_ID));
                    userMediaDetail.put(TAG_USERNAME, jsonObj.getString(TAG_USERNAME));
                    userMediaDetail.put(TAG_MEDIA_TYPE, jsonObj.getString(TAG_MEDIA_TYPE));
                    userMediaDetail.put(TAG_MEDIA_URL, jsonObj.getString(TAG_MEDIA_URL));
                    userMediaDetail.put(TAG_TIMESTAMP, jsonObj.getString(TAG_TIMESTAMP));
                } catch (Exception ex) {
                    what = WHAT_ERROR;
                    ex.printStackTrace();
                }
                mProgress.dismiss();
                handler.sendMessage(handler.obtainMessage(what, 4, 0));
            }
        }.start();

    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WHAT_ERROR) {
                mProgress.dismiss();
                if (msg.arg1 == 1) {
                    mListener.onFail("Failed to get access token");
                } else if (msg.arg1 == 2) {
                    mListener.onFail("Failed to get user information");
                }
            } else if (msg.what == WHAT_FETCH_INFO) {
                mProgress.dismiss();
                mListener.onSuccess();
            }
        }
    };

    public HashMap<String, String> getUserInfo() {
        return userInfo;
    }
    public HashMap<String, String> getUserMediaDetail() {
        return userMediaDetail;
    }
    public  ArrayList<String> getUserMediaList() {
        return userMediaList;
    }

    public boolean hasAccessToken() {
        return (mAccessToken == null) ? false : true;
    }

    public void setListener(OAuthAuthenticationListener listener) {
        mListener = listener;
    }

    public String getUserName() {
        return mSession.getUsername();
    }

    public String getId() {
        return mSession.getId();
    }

    public String getName() {
        return mSession.getName();
    }

    public String getTOken() {
        return mSession.getAccessToken();
    }

    public void authorize() {
        mDialog.show();
    }


    public void resetAccessToken() {
        if (mAccessToken != null) {
            mSession.resetAccessToken();
            mAccessToken = null;
        }
    }

    public interface OAuthAuthenticationListener {
        public abstract void onSuccess();

        public abstract void onFail(String error);
    }


}
