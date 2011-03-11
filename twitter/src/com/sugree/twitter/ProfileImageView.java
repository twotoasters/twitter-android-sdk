/*
 * Copyright 2011 Thilo Planz
 * https://github.com/thiloplanz/twitter-android-sdk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sugree.twitter;

import java.util.concurrent.Future;

import twitter4j.User;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.ImageView;


/**
 * 
 * An ImageView that shows a Twitter user's profile image.
 * 
 * <p>The image is loaded asynchronously (potentially
 * using a local cache).
 * 
 * <pre>
 *  // sample usage:
 *  
 *  // create only one shared AsyncLoader for all profile
 *  // images if you have many of them, so that they can
 *  // share the cache 
 *  AsyncLoader loader = new AsyncLoader(new Handler());
 *  
 *  ImageView t = new ProfileImageView(context,
 *                      loader, user, R.drawable.twitterIcon);
 *
 * </pre>
 * 
 * 
 * @author Thilo Planz
 *
 */


public class ProfileImageView extends ImageView {

	/**
	 * 
	 * @param context
	 * @param loader
	 * @param user
	 * @param placeholderIconWhileLoading resource id for the image to show while loading 
	 */
	public ProfileImageView(Context context, AsyncLoader loader, User user, int placeholderIconWhileLoading) {
		super(context);
		final Future<Bitmap> b = loader.loadProfileImage(user);
		if (b.isDone()){
			try {
				setImageBitmap(b.get());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		setImageResource(placeholderIconWhileLoading);
		final Handler h = loader.getHandler();
		h.postDelayed(new Runnable(){

			public void run() {
				if (!b.isDone()){
					h.postDelayed(this, 100);
					return;
				}
				try {
					Bitmap bitmap = b.get();
					setImageBitmap(bitmap);
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
			
		}, 250);
		
	}

}
