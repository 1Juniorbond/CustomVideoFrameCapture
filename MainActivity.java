package com.example.photo_get;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_VIDEO_FILE = 1;
    public Uri selectedVideoUri;
    private TextView selectedVideoTextView;
    private TextView videoInfoTextView;
    private EditText intervalEditText;
    private EditText lastMinutes;

//    public void gotoCustomScreenshotPage(View view) {
//        Intent intent = new Intent(this, MainActivity2.class);
//        startActivity(intent);
//    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化视图
        selectedVideoTextView = findViewById(R.id.selectedVideoTextView);
        videoInfoTextView = findViewById(R.id.videoInfoTextView);
        intervalEditText = findViewById(R.id.intervalEditText);
        lastMinutes = findViewById(R.id.lastMinutes);

//        // 设置选择视频按钮的点击监听器
//        Button selectVideoButton = findViewById(R.id.selectVideoFile);
//        selectVideoButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                selectVideoFile();
//            }
//        });
//
//        // 设置显示视频信息按钮的点击监听器
//        Button showVideoInfoButton = findViewById(R.id.button_showVideoInfo);
//        showVideoInfoButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    showVideoInfo();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        });
    }

    // 选择视频文件的方法
    public void selectVideoFile(View view) {
        //创建一个新的 Intent 对象，该对象指定了要执行的操作，即 Intent.ACTION_OPEN_DOCUMENT，这表示打开一个文档，允许用户选择一个文件。
        //
        //使用 setType() 方法将文件类型限制为视频文件，"video/*" 表示任何类型的视频文件。这样做可以确保用户只能选择视频文件。
        //
        //调用 startActivityForResult() 方法来启动系统 Activity 以选择视频文件。startActivityForResult() 方法允许您启动另一个 Activity，并在其完成后接收结果。
        //
        //在启动 Activity 后，系统会等待用户选择文件，并返回选定的文件 URI。当用户选择文件并且 Activity 关闭时，onActivityResult() 方法会被调用，您可以在其中处理返回的结果。
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("video/*");
        startActivityForResult(intent, REQUEST_VIDEO_FILE);
        //startActivityForResult(intent, REQUEST_VIDEO_FILE) 是一个方法调用，它启动了一个新的Activity，
        // 其中intent是用于描述将要启动的Activity的意图。
        //REQUEST_VIDEO_FILE 是一个请求代码，用于在之后识别这个特定的请求
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //onActivityResult() 方法在一个 Activity 启动另一个 Activity 并且被启动的 Activity 关闭后被调用
        //启动一个用于选择视频文件的 Activity，并且用户成功地选择了一个视频文件并返回时，系统会调用 onActivityResult() 方法
        //在这个方法中，系统会检查返回的结果，如果结果符合您预期（即请求码为 REQUEST_VIDEO_FILE，结果码为 RESULT_OK，且返回的数据不为空），
        // 则会获取用户选择的视频文件的 URI，并在界面上显示选定视频文件的名称。
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VIDEO_FILE && resultCode == RESULT_OK && data != null) {
            // 获取用户选择的视频文件的 URI
            selectedVideoUri = data.getData();
            // 获取视频文件名
            String videoName = getVideoFileName(this,selectedVideoUri);
            // 在 TextView 中显示选定视频文件名称
            selectedVideoTextView.setText("您选择了视频：" + videoName);
        }
    }
    // 显示视频信息的方法
    public void showVideoInfo(View view) throws IOException {
        if (selectedVideoUri != null) {
            // 获取视频文件的总时长和总大小
            long durationInMillis = getVideoDurationInMillis(this,selectedVideoUri);
            long fileSizeInBytes = getVideoFileSizeInBytes(selectedVideoUri);

            // 将视频时长转换为时分秒格式
            String durationFormatted = formatDuration(durationInMillis);

            // 将视频文件大小转换为以MB为单位，并保留两位小数
            double fileSizeInMB = fileSizeInBytes / (1024.0 * 1024.0);
            DecimalFormat decimalFormat = new DecimalFormat("#0.00");
            String fileSizeFormatted = decimalFormat.format(fileSizeInMB) + " MB";

            // 构建视频信息字符串
            String videoInfo = "视频总时长：" + durationFormatted + "\n" + "视频总大小：" + fileSizeFormatted;

            // 在 TextView 中显示视频信息
            videoInfoTextView.setText(videoInfo);
        } else {
            // 如果用户没有选择视频文件，则显示提示信息
            Toast.makeText(this, "请先选择视频文件", Toast.LENGTH_SHORT).show();
        }
    }



    // 获取视频文件名的方法
    public static String getVideoFileName(Context context, Uri uri) {
        String fileName = null;
        String[] filePathColumn = {MediaStore.Video.Media.DISPLAY_NAME};
        Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            fileName = cursor.getString(columnIndex);
            cursor.close();
        }
        if (fileName != null && fileName.contains(".")) {
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        return fileName;
    }


    // 获取视频文件时长的方法
    public static long getVideoDurationInMillis(Context context, Uri videoUri) throws IOException {
        long duration = 0;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, videoUri);
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            duration = Long.parseLong(durationStr);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        return duration;
    }


    // 获取视频文件大小的方法
    private long getVideoFileSizeInBytes(Uri videoUri) {
        Cursor cursor = getContentResolver().query(videoUri, null, null, null, null);
        long size = 0;
        if (cursor != null) {
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            cursor.moveToFirst();
            size = cursor.getLong(sizeIndex);
            cursor.close();
        }
        return size;
    }

    // 将毫秒转换为时分秒格式的方法


    // 退出应用的方法
//    public void exitApp(View view) {
//        finishAffinity();
//    }
    public void gotoCustomScreenshotPage(View view) {
        // 创建一个 Intent 对象，指定从当前 Activity 跳转到目标 Activity 的意图
        Intent intent = new Intent(this, MainActivity2.class);

        // 如果需要传递额外的数据到目标页面，可以使用 putExtra() 方法
        intent.putExtra("selectedVideoUri", selectedVideoUri);

        // 启动目标 Activity
        startActivity(intent);
    }
    public static String formatDuration(long durationInMillis) {
        long hours = TimeUnit.MILLISECONDS.toHours(durationInMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis) % TimeUnit.HOURS.toMinutes(1);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationInMillis) % TimeUnit.MINUTES.toSeconds(1);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // 截取视频帧的方法
    public void getPhoto(View view) throws IOException {
        if (selectedVideoUri != null) {
            long startTimeMillis = System.currentTimeMillis();
            // 显示当前时间
            String startTime = "开始时间：" + formatTime(startTimeMillis);

            // 获取TextView实例
            TextView currentTimeTextView = findViewById(R.id.currentTimeTextView);
            // 设置TextView可见
            currentTimeTextView.setVisibility(View.VISIBLE);
            // 设置TextView文本为当前时间
            currentTimeTextView.setText(startTime);

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this, selectedVideoUri);

            // 获取用户输入的截图间隔
            int intervalSeconds = Integer.parseInt(intervalEditText.getText().toString());
            long intervalUs = intervalSeconds * 1000000; // 将秒转换为微秒

            // 获取视频时长
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long duration = Long.parseLong(durationStr) * 1000; // 视频时长，单位为毫秒

            // 循环截取视频帧
            for (long timeUs = 0; timeUs < duration; timeUs += intervalUs) {
                // 获取当前时间的视频帧
                Bitmap bitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

                // 保存截图到文件
                saveBitmapToStorage(bitmap, timeUs / 1000000); // 保存为秒级别的文件名
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
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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

    // 截取最后X分钟的视频帧的方法
    public void getPhotoLast(View view) throws IOException {
        if (selectedVideoUri != null) {
            long startTimeMillis = System.currentTimeMillis();
            // 显示当前时间
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

            int last_minutes = Integer.parseInt(lastMinutes.getText().toString());//获取用户输入的截取时长
            // 计算起始时间（视频总时长减去X分钟的微秒数）
            long startTime = duration - TimeUnit.MINUTES.toMicros(last_minutes);

            // 获取用户输入的截图间隔
            int intervalSeconds = Integer.parseInt(intervalEditText.getText().toString());
            long intervalUs = intervalSeconds * 1000000; // 将秒转换为微秒

            // 循环截取视频帧
            for (long timeUs = Math.max(startTime, 0); timeUs < duration; timeUs += intervalUs) {
                // 获取当前时间的视频帧
                Bitmap bitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

                // 保存截图到文件
                saveBitmapToStorageLast(bitmap, timeUs / 1000000); // 保存为秒级别的文件名
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
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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

    // 将时间戳转换为可读格式的方法
    private String formatTime(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(millis));
    }


    // 保存 Bitmap 到存储空间的方法
    private void saveBitmapToStorage(Bitmap bitmap, long timeSeconds) {
        // 获取外部存储的 DCIM 目录
        String videoName = getVideoFileName(this,selectedVideoUri);
        File dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

        // 创建以视频文件名命名的子目录
        File photoSaveDir = new File(dcimDir, videoName);
        if (!photoSaveDir.exists()) {
            photoSaveDir.mkdirs(); // 如果目录不存在，则创建
        }

        // 文件名
        String fileName = videoName + "_" + timeSeconds + ".jpg";

        // 创建文件输出流
        File file = new File(photoSaveDir,"完整-"+ fileName);
        try {
            FileOutputStream outputStream = new FileOutputStream(file);

            // 将 Bitmap 压缩为 JPEG 格式并写入文件
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            // 关闭输出流
            outputStream.close();

            // 显示保存路径
            Toast toast = Toast.makeText(this, "截图已保存至：DCIM/" + fileName, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0); // 设置消息框位置为屏幕中央
            toast.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 保存 Bitmap 到存储空间的方法（针对最后X分钟截图）
    private void saveBitmapToStorageLast(Bitmap bitmap, long timeSeconds) {
        // 获取外部存储的 DCIM 目录
        String videoName = getVideoFileName(this,selectedVideoUri);
        File dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

        // 创建以视频文件名命名的子目录
        int lastMinutesValue = Integer.parseInt(lastMinutes.getText().toString());
        File photoSaveDir = new File(dcimDir, "最后" + lastMinutesValue + "分钟-"+videoName);
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
}
