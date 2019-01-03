package com.example.qihua.test;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Xml;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends BaseActivity {

    private EditText mId;
    private EditText mage;
    private EditText mname;
    private EditText uri;
    private TextView result;
    private static final int LOAD_SUCCESS = 1;
    private static final int LOAD_ERROR = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mId = (EditText) findViewById(R.id.id);
        mage = (EditText) findViewById(R.id.age);
        mname = (EditText) findViewById(R.id.name);
        uri = (EditText) findViewById(R.id.uri);
        result = (TextView) findViewById(R.id.textview);
    }

    /**
     * 读取流的数据返回一个字符串
     */
    public String readStream(InputStream is) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            is.close();
            String result = baos.toString();
            if (result.contains("gb2312")) {
                return baos.toString("gb2312");
            } else if (result.contains("gbk")) {
                return baos.toString("gbk");
            } else {
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_SUCCESS:
                    String str = (String) msg.obj;
                    result.setText(str);
                    System.out.println("str:"+str);
                    break;
                case LOAD_ERROR:
                    Toast.makeText(MainActivity.this, "加载失败, code:" +
                            msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 确定点击事件
     *
     * @param view
     */
    public void queding(View view) {
        final String url = uri.getText().toString().trim();
        //线程中进行
        new Thread() {
            @Override
            public void run() {
                try {
                    URL url1 = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection) url1.openConnection();
                    int code = connection.getResponseCode();
                    System.out.println("code:"+code);
                    if (code == 200) {
                        InputStream is = connection.getInputStream();
                        String text = readStream(is);
                        Message message = Message.obtain();
                        message.what = LOAD_SUCCESS;
                        message.obj = text;
//                        Looper.prepare();
//                        Looper.loop();
                        mHandler.sendMessage(message);

                    } else {
                        Message message = Message.obtain();
                        message.what = LOAD_ERROR;
                        message.obj = "code:" + code;
                        mHandler.sendMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = LOAD_ERROR;
                    message.obj = -1;
                    mHandler.sendMessage(message);
                }
            }
        }.start();
    }

    /*
    保存
     */
    public void baocun(View view) {
        final String id = mId.getText().toString().trim();
        final String age = mage.getText().toString().trim();
        final String name = mname.getText().toString().trim();
        performCodeWithPermission("保存到SD卡中", new PermissionCallback() {
            @Override
            public void hasPermission() {
                XmlSerializer serializer = Xml.newSerializer();
                FileOutputStream outputStream = null;
                try {
                    outputStream = openFileOutput("info.xml", Context.MODE_PRIVATE);
                    serializer.setOutput(outputStream, "utf-8");
                    serializer.startDocument("utf-8", true);
                    serializer.startTag(null, "info");
                    serializer.startTag(null, "student");
                    serializer.attribute(null, "id", id);
                    serializer.startTag(null, "name");
                    serializer.text(name);
                    serializer.endTag(null, "name");
                    serializer.startTag(null, "age");
                    serializer.text(age);
                    serializer.endTag(null, "age");
                    serializer.startTag(null, "id");
                    serializer.text(id);
                    serializer.endTag(null, "id");
                    serializer.endTag(null, "student");
                    serializer.endTag(null, "info");
                    serializer.endDocument();
                    Toast.makeText(MainActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }

            @Override
            public void noPermission() {
                Toast.makeText(MainActivity.this, "没有获得写入SD卡的权限", Toast.LENGTH_SHORT).show();
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    }

    /**
     * 解析
     *
     * @param view
     */
    public void jiexi(View view) {
        try {
            // InputStream inputStream = getAssets().open("getWeatherbyCityName.xml");
            FileInputStream inputStream = openFileInput("info.xml");
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(inputStream, "utf-8");
            int type = parser.getEventType();
            while (type != XmlPullParser.END_DOCUMENT) {
                System.out.println(parser.getEventType() + "----" + parser.getName() + "----" + parser.getText());
                type = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void read(View view) {
        read();
    }

    public void write(View view) {
        write();
    }

    private void read() {
        try {
            FileInputStream fis = openFileInput("info.xml");
            byte[] bytes = new byte[20];
            fis.read(bytes);
            Toast.makeText(this, "" + new String(bytes), Toast.LENGTH_SHORT).show();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    写
     */
    private void write() {
        try {
            FileOutputStream fos = openFileOutput("file.txt", Context.MODE_PRIVATE);
            fos.write("data".getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
