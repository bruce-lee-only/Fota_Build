/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.util.svr;

import com.momock.http.wget.SpeedInfo;
import com.momock.http.wget.WGet;
import com.momock.http.wget.info.DownloadInfo;
import com.momock.http.wget.info.DownloadInfo.Part;
import com.momock.http.wget.info.ex.DownloadMultipartError;
import com.momock.util.Logger;

import java.io.File;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

public class WGetHelper {

    public static String formatSpeed(int bytes) {
        String str = "";
        float speed = bytes;
        if (speed < 1000000) {
            speed /= 1024;
            str += String.format("%.02f", speed) + " KB/s";
        } else {
            speed /= 1024 * 1024;
            str += String.format("%.02f", speed) + " MB/s";
        }
        return str;
    }

	public static long download(final String url, File file){
		return download(url, file, 60);
	}
	public static long download(final String url, File file, final int connTimeout){
		if (url == null || file == null) return -1;
		try {
			final long start = System.currentTimeMillis();
			final AtomicBoolean connecting = new AtomicBoolean(true);
			final AtomicBoolean stop = new AtomicBoolean(false);
			final DownloadInfo info = new DownloadInfo(new URL(url));
			final SpeedInfo speedInfo = new SpeedInfo();

			Runnable notify = new Runnable() {
				long last = 0;
				@Override
				public void run() {
					long delta;
					switch (info.getState()) {
					case EXTRACTING:
						Logger.debug(url + ":" + info.getState());
						break;
					case EXTRACTING_DONE:
						Logger.debug(url + ":" + info.getState());
						connecting.set(false);
						break;
					case DONE:
						Logger.debug(url + ":" + info.getState());
						break;
					case RETRYING:
						delta = (System.currentTimeMillis() - start) / 1000;
						if (connecting.get() && connTimeout > 0 && delta > connTimeout){
							Logger.debug(url + ": connecting time out ! (" + delta + "s)");
							stop.set(true);
						}
						Logger.debug(url + ":" + info.getState() + " " + info.getDelay() + " (" + delta + "s)");
						break;
					case DOWNLOADING:
						speedInfo.step(info.getCount());
                        long now = System.currentTimeMillis();
                        if (now - 1000 > last) {
                            last = now;
                            float p = info.getCount() / (float) info.getLength();

							Logger.debug(url + ":" + (int)(p * 100) + "% (" + formatSpeed(speedInfo.getCurrentSpeed()) + "/" + formatSpeed(speedInfo.getAverageSpeed()) + ")");
                        }
						break;
					default:
						break;
					}
				}
			};
			info.extract(stop, notify);
			if (info.getLength() == null || info.getLength() <= 0) return -1;
			info.enableMultipart();
			if (file.exists())
				file.delete();
			WGet w = new WGet(info, file);
			speedInfo.start(0);
			w.download(stop, notify);
			if (info.getCount() == info.getLength())
				return info.getLength();
			else {
				if (file.exists())
					file.delete();
				return -1;
			}
		} catch (DownloadMultipartError e) {
			Logger.error(e);
			for (Part p : e.getInfo().getParts()) {
				Throwable ee = p.getException();
				if (ee != null)
					ee.printStackTrace();
			}
		} catch (RuntimeException e) {
			Logger.error(e);
		} catch (Exception e) {
			Logger.error(e);
		}
		return -1;
	}

}
