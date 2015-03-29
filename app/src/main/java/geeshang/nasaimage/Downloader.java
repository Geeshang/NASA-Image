package geeshang.nasaimage;
/****************************************************************************
 Copyright (c) 2015 Geeshang Xu (Geeshangxu@gmail.com)

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ****************************************************************************/

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * This class encapsulate some network operations  use "Get" method through Http protocol.
 */
class Downloader {
    private final MainActivity mainActivity;

    Downloader(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public String downloadString(String url) {
        String result = "";
        String readLine;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(download(url)));
        try {
            while ((readLine = bufferedReader.readLine()) != null) {
                result += readLine;
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Bitmap downloadImage(String url) {
        return BitmapFactory.decodeStream(download(url));
    }


    public InputStream download(String url) {
        InputStream is = null;
        try {
            URL url_obj = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url_obj.openConnection();
            httpURLConnection.setConnectTimeout(3000);
            httpURLConnection.setRequestMethod("GET");
            is = httpURLConnection.getInputStream();
        } catch (SocketTimeoutException e) {
            mainActivity.myHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mainActivity, mainActivity.getString(R.string.network_times_out), Toast.LENGTH_SHORT)
                            .show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (is == null) {
            mainActivity.myHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mainActivity, mainActivity.getString(R.string.failed_fetch_net_resource),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
        return is;
    }
}
