package com.sugree.twitter;

import java.io.IOException;
import java.net.MalformedURLException;

import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieSyncManager;

public class Twitter {
	public static final String TAG = "twitter";
	
	public static final String CALLBACK_URI = "twitter://callback";
	public static final String CANCEL_URI = "twitter://cancel";
	public static final String ACCESS_TOKEN = "access_token";
	public static final String SECRET_TOKEN = "secret_token";

	public static final String REQUEST = "request";
	public static final String AUTHORIZE = "authorize";

	protected static String REQUEST_ENDPOINT = "https://api.twitter.com/1";
	
	protected static String OAUTH_REQUEST_TOKEN = "https://api.twitter.com/oauth/request_token";
	protected static String OAUTH_ACCESS_TOKEN = "https://api.twitter.com/oauth/access_token";
	protected static String OAUTH_AUTHORIZE = "https://api.twitter.com/oauth/authorize";
	
	private boolean accessTokenSet = false;
	
	private int mIcon;
	private twitter4j.Twitter mTwitter;
	
	public Twitter(int icon, String consumerKey, String consumerSecret) {
		mIcon = icon;
	    mTwitter = new TwitterFactory().getInstance();
	    mTwitter.setOAuthConsumer(consumerKey, consumerSecret);
	}
	
	public void authorize(Context ctx,
			final DialogListener listener) {
		CookieSyncManager.createInstance(ctx);
		dialog(ctx, new DialogListener() {

			public void onComplete(Bundle values) {
				CookieSyncManager.getInstance().sync();
				mTwitter.setOAuthAccessToken(new AccessToken(values.getString(ACCESS_TOKEN), values.getString(SECRET_TOKEN)));
				if (isSessionValid()) {
					Log.d(TAG, "token "+values.getString(ACCESS_TOKEN)+" "+values.getString(SECRET_TOKEN));
					listener.onComplete(values);
				} else {
					onTwitterError(new TwitterError("failed to receive oauth token"));
				}
			}

			public void onTwitterError(TwitterError e) {
				Log.d(TAG, "Login failed: "+e);
				listener.onTwitterError(e);
			}

			public void onError(DialogError e) {
				Log.d(TAG, "Login failed: "+e);
				listener.onError(e);
			}

			public void onCancel() {
				Log.d(TAG, "Login cancelled");
				listener.onCancel();
			}
			
		});
	}
	
	public String logout(Context context) throws MalformedURLException, IOException {
		return "true";
	}
	
	public void dialog(final Context ctx,
			final DialogListener listener) {
		if (ctx.checkCallingOrSelfPermission(Manifest.permission.INTERNET) !=
			PackageManager.PERMISSION_GRANTED) {
			Util.showAlert(ctx, "Error", "Application requires permission to access the Internet");
			return;
		}
		new TwDialog(ctx, mTwitter,
				listener, mIcon).show();
	}
	
	public boolean isSessionValid() {
		return accessTokenSet;
	}

	public void setOAuthAccessToken(String accessToken, String secretToken) {
	    mTwitter.setOAuthAccessToken(new AccessToken(accessToken, secretToken));
	    accessTokenSet = true;
	}
	
	public void updateStatus(String statusString) {
	    try {
	        mTwitter.updateStatus(statusString);
	    } catch (TwitterException e) {
	        Log.e(TAG, e.toString());
	    }
	}
	
	public twitter4j.Twitter getTwitter() {
	    return mTwitter;
	}
	
	public static interface DialogListener {
		public void onComplete(Bundle values);
		public void onTwitterError(TwitterError e);
		public void onError(DialogError e);
		public void onCancel();
	}
}
