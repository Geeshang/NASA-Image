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


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class MainActivity extends Activity implements View.OnTouchListener, View.OnClickListener,
        View.OnLongClickListener, SwipeRefreshLayout.OnRefreshListener {
    //Use translation service from BaiDu.
    private static final String BAIDU_TRANSLATE_URL =
            "http://openapi.baidu.com/public/2.0/bmt/translate?client_id=13jrLBe0N9XDdfDmR9n6LBAm&q=";
    private static final String NASA_URL = "http://www.nasa.gov/rss/dyn/image_of_the_day.rss";
    //Use the path below to save image.
    static final String SAVE_PATH = Environment.getExternalStorageDirectory() + "/NASAImage";
    StringBuilder[] title_data = new StringBuilder[30];
    StringBuilder[] date_data = new StringBuilder[30];
    StringBuilder[] description_data = new StringBuilder[30];
    String[] image_url = new String[30];
    private int dayIndex;
    final Handler myHandler = new Handler();
    private final Downloader downloader = new Downloader(this);
    private final ExternalStorageHandler externalStorageHandler = new ExternalStorageHandler(this);
    private DataReloader dataReloader;
    private ProgressBar progressBar;
    private TextView loading, titleView, dateView, descriptionView;
    private ImageView imageView;
    private Bitmap image_data;
    private String targetLanguage;
    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findId();
        initDataArray();
        externalStorageHandler.startObservingExternalStorage();
        downloadOrReload();
        registerListener();
        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_dark);
    }

    void findId() {
        progressBar = (ProgressBar) findViewById(R.id.id_progressbar);
        loading = (TextView) findViewById(R.id.id_loading);
        titleView = (TextView) findViewById(R.id.id_title);
        dateView = (TextView) findViewById(R.id.id_date);
        imageView = (ImageView) findViewById(R.id.id_image);
        descriptionView = (TextView) findViewById(R.id.id_description);
    }

    private void registerListener() {
        ImageButton back = (ImageButton) findViewById(R.id.id_back);
        ImageButton translate = (ImageButton) findViewById(R.id.id_translate);
        ImageButton forward = (ImageButton) findViewById(R.id.id_forward);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        imageView.setOnClickListener(this);
        back.setOnClickListener(this);
        back.setOnTouchListener(this);
        translate.setOnClickListener(this);
        translate.setOnTouchListener(this);
        translate.setOnLongClickListener(this);
        forward.setOnClickListener(this);
        forward.setOnTouchListener(this);
        refreshLayout.setOnRefreshListener(this);
    }

    private void initDataArray() {
        for (int i = 0; i < title_data.length; i++) {
            title_data[i] = new StringBuilder();
            date_data[i] = new StringBuilder();
            description_data[i] = new StringBuilder();
        }
    }

    private void downloadOrReload() {
        FragmentManager fragmentManager = getFragmentManager();
        dataReloader = (DataReloader) fragmentManager.findFragmentByTag("dataReloader");
        //To prevent MainActivity restart from rotating the screen,
        //if dataReloader is null, download the data and store in the DataReloader.
        if (dataReloader == null) {
            download();
            dataReloader = new DataReloader();
            fragmentManager.beginTransaction().add(dataReloader, "dataReloader").commit();
            dataReloader.setData(this);
        } else {
            title_data = dataReloader.getData().title_data;
            date_data = dataReloader.getData().date_data;
            image_url = dataReloader.getData().image_url;
            image_data = dataReloader.getData().image_data;
            dayIndex = dataReloader.getData().dayIndex;
            description_data = dataReloader.getData().description_data;
            setProgressBarGone();
            resetView();
        }
    }

    private void download() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                MyDefaultHandler defaultHandler = new MyDefaultHandler(MainActivity.this);
                try {
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser parser = factory.newSAXParser();
                    InputStream inputStream = downloader.download(NASA_URL);
                    if (inputStream != null) {
                        parser.parse(inputStream, defaultHandler);
                    }
                } catch (ParserConfigurationException | SAXException | IOException e) {
                    e.printStackTrace();
                }
                MainActivity.this.myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        resetView();
                    }
                });
                getImage();
                setProgressBarGone();
                MainActivity.this.myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        resetView();
                    }
                });
            }
        }.start();
    }

    private void getImage() {
        //To judge whether date_data[dayIndex] have nothing or not.
        if (!date_data[dayIndex].toString().equals("")) {
            //If there already has the image in SD card ,just fetch from it.
            File file = new File(SAVE_PATH, externalStorageHandler.removeSpaceChar
                    (title_data[dayIndex].toString()) + ".jpeg");
            if (externalStorageHandler.hasFileExists(file)) {
                image_data = BitmapFactory.decodeStream(externalStorageHandler.readFileFromExternalStorage(file));
                resetViewOtherThread();
            } else {
                resetViewOtherThread();
                setProgressBarShow("withText");
                image_data = downloader.downloadImage(image_url[dayIndex]);
            }
        }
    }

    void setProgressBarGone() {
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                loading.setVisibility(View.GONE);
                titleView.setVisibility(View.VISIBLE);
                dateView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.VISIBLE);
                descriptionView.setVisibility(View.VISIBLE);
            }
        });
    }

    void setProgressBarShow(String mode) {
        switch (mode) {
            case "only":
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                        loading.setVisibility(View.VISIBLE);
                        titleView.setVisibility(View.GONE);
                        dateView.setVisibility(View.GONE);
                        imageView.setVisibility(View.GONE);
                        descriptionView.setVisibility(View.GONE);
                    }
                });
                break;
            case "withText":
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                        loading.setVisibility(View.VISIBLE);
                        titleView.setVisibility(View.VISIBLE);
                        dateView.setVisibility(View.VISIBLE);
                        imageView.setVisibility(View.GONE);
                        descriptionView.setVisibility(View.VISIBLE);
                    }
                });
                break;
            case "withImage":
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                        loading.setVisibility(View.VISIBLE);
                        titleView.setVisibility(View.GONE);
                        dateView.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        descriptionView.setVisibility(View.GONE);
                    }
                });
                break;
        }

    }

    private void resetView() {
        titleView.setText(title_data[dayIndex]);
        dateView.setText(date_data[dayIndex]);
        imageView.setImageBitmap(image_data);
        descriptionView.setText("\u3000\u3000" + description_data[dayIndex]);
    }

    void resetViewOtherThread() {
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                resetView();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataReloader.setData(this);
        externalStorageHandler.stopObservingExternalStorage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_save:
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        saveImage();
                    }
                }.start();
                break;
            case R.id.action_share_text:
                showShare();
                break;
            case R.id.action_share_image:
                showShare();
                break;
            case R.id.action_share_text_image:
                showShare();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveImage() {
        if (image_data != null) {
            externalStorageHandler.saveFileToExternalStorage(image_data,
                    externalStorageHandler.removeSpaceChar(title_data[dayIndex].toString()) + ".jpeg", false);
        } else {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, getString(R.string.wait_image_loading), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void showShare() {
        //Here to implement your share feature
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, getString(R.string.share_service_not_available), Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_back:
                if (dayIndex == 30) {
                    Toast.makeText(this, getString(R.string.reach_the_end), Toast.LENGTH_SHORT).show();
                } else {
                    dayIndex++;
                    image_data = null;
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            getImage();
                            setProgressBarGone();
                            resetViewOtherThread();
                        }
                    }.start();
                }
                break;
            case R.id.id_translate:
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        setProgressBarShow("withImage");
                        baiDuTranslate("zh");
                        setProgressBarGone();
                        resetViewOtherThread();
                    }
                }.start();
                break;
            case R.id.id_forward:
                if (dayIndex == 0) {
                    Toast.makeText(this, getString(R.string.newest_info), Toast.LENGTH_SHORT).show();
                } else {
                    dayIndex--;
                    image_data = null;
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            getImage();
                            resetViewOtherThread();
                            setProgressBarGone();
                        }
                    }.start();
                }
                break;
            case R.id.id_image:
                Intent intent = new Intent(this, ImageOnlyActivity.class);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                image_data.compress(Bitmap.CompressFormat.JPEG, 100, os);
                byte[] imageBytes = os.toByteArray();
                intent.putExtra("large_image", imageBytes);
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        LanguageSelectDialog dialog = new LanguageSelectDialog();
        dialog.show(getFragmentManager(), "LanguageSelectDialog");
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            v.setAlpha(0);
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            v.setAlpha(255);
        }
        return false;
    }

    @Override
    public void onRefresh() {
        //wipe data already exist
        initDataArray();
        download();
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

    private void setTargetLanguage(int index) {
        String[] languages = {"zh", "en", "kor", "jp", "fr", "ru", "de", "spa"};
        this.targetLanguage = languages[index];
    }

    private void baiDuTranslate(String languageTo) {
        JsonParser jsonParser = new JsonParser();
        String titleJson;
        String descriptionJson;
        String titleEncoded = null, descriptionEncoded = null;
        //Encode the translation content to UTF-8.
        try {
            titleEncoded = URLEncoder.encode(title_data[dayIndex].toString(), "utf-8");
            descriptionEncoded = URLEncoder.encode(description_data[dayIndex].toString(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //Request translation and parse the result.
        titleJson = downloader.downloadString(BAIDU_TRANSLATE_URL + titleEncoded
                + "&from=auto" + "&to=" + languageTo);
        descriptionJson = downloader.downloadString(BAIDU_TRANSLATE_URL + descriptionEncoded
                + "&from=auto" + "&to=" + languageTo);
        try {
            title_data[dayIndex] = jsonParser.parse(titleJson);
            description_data[dayIndex] = jsonParser.parse(descriptionJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Decode the translation result which in UTF-8.
        try {
            title_data[dayIndex] = new StringBuilder(URLDecoder.decode(title_data[dayIndex].toString(), "utf-8"));
            description_data[dayIndex] =
                    new StringBuilder(URLDecoder.decode(description_data[dayIndex].toString(), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("ValidFragment")
    public class LanguageSelectDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            setRetainInstance(true);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.select_language));
            builder.setItems(R.array.array_language, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setTargetLanguage(which);
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            setProgressBarShow("withImage");
                            baiDuTranslate(targetLanguage);
                            setProgressBarGone();
                            resetViewOtherThread();
                        }
                    }.start();
                }
            });
            return builder.create();
        }

    }
}
