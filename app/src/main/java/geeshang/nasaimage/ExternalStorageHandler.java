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

package geeshang.nasaimage;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class encapsulate some external storage operations.
 */
class ExternalStorageHandler {
    private final MainActivity mMainActivity;
    private BroadcastReceiver mExternalStorageReceiver;
    private boolean mExternalStorageAvailable = false;
    private boolean mExternalStorageWriteable = false;

    ExternalStorageHandler(MainActivity mainActivity) {
        this.mMainActivity = mainActivity;
    }

    void saveFileToExternalStorage(Bitmap bitmap, String fileName, boolean append) {
        if (mExternalStorageWriteable) {
            if (append) {
                //Left to implement the append mode
            } else {
                if (hasFileExists(MainActivity.SAVE_PATH, fileName)) {
                    mMainActivity.myHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mMainActivity, mMainActivity.getString(R.string.image_already_saved), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    File path = new File(MainActivity.SAVE_PATH);
                    path.mkdirs();
                    File file = new File(path, fileName);
                    try {
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                        bos.flush();
                        bos.close();
                        mMainActivity.myHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mMainActivity, mMainActivity.getString(R.string.save_succeed), Toast.LENGTH_SHORT).show();
                            }
                        });
                        //Refresh image gallery index
                        MediaScannerConnection.scanFile(mMainActivity,
                                new String[]{file.toString()}, null,
                                new MediaScannerConnection.OnScanCompletedListener() {
                                    public void onScanCompleted(String path, Uri uri) {
                                    }
                                });
                    } catch (IOException e) {
                        mMainActivity.myHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mMainActivity, mMainActivity.getString(R.string.save_failed), Toast.LENGTH_SHORT).show();
                            }
                        });
                        e.printStackTrace();
                    }
                }
            }
        } else {
            mMainActivity.myHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mMainActivity, mMainActivity.getString(R.string.save_failed_no_Sdcard), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    InputStream readFileFromExternalStorage(File file) {
        FileInputStream is = null;
        if (mExternalStorageAvailable) {
            try {
                is = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return is;
    }

    boolean hasFileExists(String filePath, String fileName) {
        if (mExternalStorageAvailable) {
            File file = new File(filePath, fileName);
            return file.exists();
        } else {
            mMainActivity.myHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mMainActivity, mMainActivity.getString(R.string.no_Sdcard_exist), Toast.LENGTH_LONG).show();
                }
            });
            return false;
        }
    }

    //Overloaded method.
    boolean hasFileExists(File file) {
        if (mExternalStorageAvailable) {
            return file.exists();
        } else {
            mMainActivity.myHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mMainActivity, mMainActivity.getString(R.string.no_Sdcard_exist), Toast.LENGTH_LONG).show();
                }
            });
            return false;
        }
    }

    void startObservingExternalStorage() {
        mExternalStorageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateExternalStorage();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        mMainActivity.registerReceiver(mExternalStorageReceiver, filter);
        updateExternalStorage();
    }

    void updateExternalStorage() {
        String state = Environment.getExternalStorageState();
        switch (state) {
            case Environment.MEDIA_MOUNTED:
                mExternalStorageAvailable = mExternalStorageWriteable = true;
                break;
            case Environment.MEDIA_MOUNTED_READ_ONLY:
                mExternalStorageAvailable = true;
                mExternalStorageWriteable = false;
                break;
            default:
                mExternalStorageAvailable = mExternalStorageWriteable = false;
                break;
        }
    }

    void stopObservingExternalStorage() {
        mMainActivity.unregisterReceiver(mExternalStorageReceiver);
    }

    //Remove space character in string
    String removeSpaceChar(String title) {
        char[] newTitle = title.toCharArray();
        int length = newTitle.length;
        int newLength = 0;
        for (int i = 0; i < length; i++) {
            char ch = newTitle[i];
            if (ch == ' ') {
                //Do nothing
            } else {
                newTitle[newLength++] = ch;
            }
        }
        //TODO:--newLength seems wield, but code works only in this form.Left to find why.
        return new String(newTitle, 0, --newLength);
    }
}

