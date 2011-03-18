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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import twitter4j.User;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

/**
 * Helper class that manages a thread and a cache to asynchronously load Twitter
 * profile images to be displayed in an ImageView.
 * 
 * <p>
 * It also wraps a Handler to update the UI components once the image has
 * loaded. This Handler (given to the constructor) must have a Looper, i.e. it
 * should be created on the main thread.
 * 
 * @see ProfileImageView
 * @author Thilo Planz
 * 
 */

public class AsyncLoader {

	private ThreadPoolExecutor thread = new ThreadPoolExecutor(0, 1, 5,
			TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

	private final HashMap<String, SoftReference<Bitmap>> cache = new HashMap<String, SoftReference<Bitmap>>();

	private final Handler handler;

	private File cacheRoot;

	/**
	 * This Handler must have a Looper, i.e. it should be created on the main
	 * thread.
	 */
	public AsyncLoader(Handler handler) {
		this.handler = handler;
	}

	/**
	 * If set, the given directory will be used for an on-disk cache for
	 * downloaded images
	 * 
	 */
	public void setCacheDir(File cacheRoot) {
		if (cacheRoot == null) {
			this.cacheRoot = cacheRoot;
			return;
		}
		if (!cacheRoot.exists()) {
			cacheRoot.mkdirs();
		}
		if (!cacheRoot.isDirectory()) {
			throw new IllegalArgumentException(cacheRoot
					+ " is not a directory");
		}
		this.cacheRoot = cacheRoot;
	}

	private Bitmap checkCache(String url) {
		SoftReference<Bitmap> cached = cache.get(url);
		if (cached != null) {
			final Bitmap result = cached.get();
			if (result != null)
				return result;
		}

		if (cacheRoot != null) {
			String cacheUrl = url.replace(":", "_").replace("/", "_");
			File f = new File(new File(cacheRoot, "com.sugree.twitter.cache"),
					cacheUrl);
			if (f.length() > 0) {
				try {
					Bitmap result = BitmapFactory.decodeFile(f
							.getAbsolutePath());
					if (result != null) {
						cache.put(url, new SoftReference<Bitmap>(result));
						return result;
					} else {
						f.delete();
					}
				} catch (Exception e) {
					e.printStackTrace();
					f.delete();
				}
			}
		}

		return null;
	}

	Future<Bitmap> loadProfileImage(User user) {

		final String url = user.getProfileImageURL().toExternalForm();

		Bitmap cached = checkCache(url);
		if (cached != null) {
			return new AlreadyDone<Bitmap>(cached);
		}

		return thread.submit(new Callable<Bitmap>() {

			public Bitmap call() throws Exception {
				Bitmap cached = checkCache(url);
				if (cached != null) {
					return cached;
				}

				HttpURLConnection connection = (HttpURLConnection) new URL(url)
						.openConnection();
				connection.setDoInput(true);
				connection.connect();
				InputStream input = connection.getInputStream();
				Bitmap result = BitmapFactory.decodeStream(input);
				connection.disconnect();

				cache.put(url, new SoftReference<Bitmap>(result));

				if (cacheRoot != null)
					try {
						String cacheUrl = url.replace(":", "_").replace("/",
								"_");
						File f = new File(cacheRoot, "com.sugree.twitter.cache");
						f.mkdirs();
						f = new File(f, cacheUrl);
						result.compress(Bitmap.CompressFormat.PNG, 100,
								new FileOutputStream(f));

					} catch (IOException e) {
						e.printStackTrace();
					}

				return result;

			}
		});
	}

	Handler getHandler() {
		return handler;
	}

}
