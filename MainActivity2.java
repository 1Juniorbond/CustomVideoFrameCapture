package com.example.photo_get;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity2 extends AppCompatActivity {
    public Uri selectedVideoUri;
    private TextView showVideoName;
    private EditText intervalEditText2;
    private EditText setStartTime;
    private EditText setEndTime;

    private TextView videoInfoTextView2;


    public void gotoHome(View view) {
        Intent intent = new Intent(this, MainActivity.class);

        startActivity(intent);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // 初始化 test TextView
        showVideoName = findViewById(R.id.showVideoName);
        setStartTime=findViewById(R.id.setStartTime);
        setEndTime=findViewById(R.id.setEndTime);
        intervalEditText2=findViewById(R.id.intervalEditText2);
        videoInfoTextView2 = findViewById(R.id.videoInfoTextView2);

        Intent intent = getIntent();

        // 使用 getIntent().getParcelableExtra("key") 方法获取附加的数据
        selectedVideoUri = intent.getParcelableExtra("selectedVideoUri");

        if (selectedVideoUri != null) {
            // 处理 selectedVideoUri
            // 获取视频文件名

            String videoName = MainActivity.getVideoFileName(this,selectedVideoUri);
            // 在 TextView 中显示选定视频文件名称
            showVideoName.setText("您选择了视频：" + videoName);


            // 获取视频文件的总时长
            long durationInMillis = 0;
            try {
                durationInMillis = MainActivity. getVideoDurationInMillis(this,selectedVideoUri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // 将视频时长转换为时分秒格式
            String durationFormatted = MainActivity.formatDuration(durationInMillis);
            // 构建视频信息字符串
            String videoInfo = "视频总时长：" + durationFormatted ;
            // 在 TextView 中显示视频信息
            videoInfoTextView2.setText(videoInfo);
        }
    }
    public void customSet(View view) throws IOException {
        if (selectedVideoUri != null) {
            long startTimeMillis = System.currentTimeMillis();
             //显示当前时间
            String startviewTime = "开始时间：" + formatTime(startTimeMillis);

            // 获取TextView实例
            TextView currentTimeTextView = findViewById(R.id.currentTimeTextView);
            // 设置TextView可见
            currentTimeTextView.setVisibility(View.VISIBLE);
            // 设置TextView文本为当前时间
            currentTimeTextView.setText(startviewTime);

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this, selectedVideoUri);

            // 获取视频总时长
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long duration = Long.parseLong(durationStr)* 1000; // 视频时长，单位为毫秒  毫秒乘1000后是微秒

            int start_setTime = Integer.parseInt(setStartTime.getText().toString());//获取用户输入的截取时长
            int end_setTime = Integer.parseInt(setEndTime.getText().toString());//获取用户输入的截取时长
            // 计算起始时间（视频总时长减去X分钟的微秒数）
            long startSETTime = TimeUnit.MINUTES.toMicros(start_setTime);
            long endSETTime = TimeUnit.MINUTES.toMicros(end_setTime);


            // 获取用户输入的截图间隔
            int intervalSeconds = Integer.parseInt(intervalEditText2.getText().toString());
            long intervalUs = intervalSeconds * 1000000; // 将秒转换为微秒

            // 循环截取视频帧
            for (long timeUs = Math.max(startSETTime, 0); timeUs < endSETTime; timeUs += intervalUs) {
                // 获取当前时间的视频帧
                Bitmap bitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

                // 保存截图到文件
                saveBitmapToStorageCustom(bitmap, timeUs / 1000000); // 保存为秒级别的文件名
            }

            long endTimeMillis = System.currentTimeMillis();
            // 显示当前时间
            String endTime = "结束时间：" + formatTime(endTimeMillis);

            // 获取TextView实例
            TextView endTimeTextView = findViewById(R.id.endTimeTextView);
            // 设置TextView可见
            endTimeTextView.setVisibility(View.VISIBLE);
            // 设置TextView文本为当前时间
            endTimeTextView.setText(endTime);

            // 创建并显示对话框
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity2.this);
            builder.setTitle("截图已完成^_^");
            builder.setMessage("enjoy your beauty O(∩_∩)O");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 用户点击确定按钮后的操作，例如关闭对话框
                    dialog.dismiss(); // 关闭对话框
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

            // 释放资源
            retriever.release();
        } else {
            Toast.makeText(this, "请先选择视频文件", Toast.LENGTH_SHORT).show();
        }
    }
    private void saveBitmapToStorageCustom(Bitmap bitmap, long timeSeconds) {
        // 获取外部存储的 DCIM 目录
        String videoName = MainActivity.getVideoFileName(this,selectedVideoUri);
        File dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

        // 创建以视频文件名命名的子目录
        int start_setTime = Integer.parseInt(setStartTime.getText().toString());//获取用户输入的截取时长
        int end_setTime = Integer.parseInt(setEndTime.getText().toString());//获取用户输入的截取时长
        File photoSaveDir = new File(dcimDir,  start_setTime+"min-" + end_setTime+"min"+"-"+videoName );
        if (!photoSaveDir.exists()) {
            photoSaveDir.mkdirs(); // 如果目录不存在，则创建
        }

        // 文件名
        String fileName = videoName + "_" + timeSeconds + ".jpg";

        // 创建文件输出流
        File file = new File(photoSaveDir, fileName);
        try {
            FileOutputStream outputStream = new FileOutputStream(file);

            // 将 Bitmap 压缩为 JPEG 格式并写入文件
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            // 关闭输出流
            outputStream.close();

            // 显示保存路径
            Toast toast = Toast.makeText(this, "截图已保存至：DCIM/" + fileName, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 0); // 设置消息框位置为屏幕中央
            toast.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String formatTime(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(millis));
    }



}
