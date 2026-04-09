package com.example.expensetrackeradmin.helpers;

import android.content.Context;
import android.net.Uri;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.expensetrackeradmin.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class CloudinaryHelper {

    private static boolean isInitialized = false;

    public static void init(Context context) {
        if (!isInitialized) {
            try {
                Map<String, String> config = new HashMap<>();

                config.put("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME);
                config.put("api_key", BuildConfig.CLOUDINARY_API_KEY);
                config.put("api_secret", BuildConfig.CLOUDINARY_API_SECRET);

                MediaManager.init(context, config);
                isInitialized = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface CloudinaryCallback {
        void onSuccess(String imageUrl);
        void onError(String errorMessage);
    }

    private static String getFilePathFromUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File tempFile = new File(context.getCacheDir(), "lapi_temp_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return tempFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void uploadImage(Context context, Uri imageUri, CloudinaryCallback callback) {
        String realFilePath = getFilePathFromUri(context, imageUri);

        if (realFilePath != null) {
            MediaManager.get().upload(realFilePath)
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            // Đang up
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String secureUrl = (String) resultData.get("secure_url");
                            callback.onSuccess(secureUrl);

                            new File(realFilePath).delete();
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            callback.onError(error.getDescription());

                            // Dọn dẹp: Lỗi cũng phải xóa rác
                            new File(realFilePath).delete();
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                        }
                    })
                    .dispatch();
        } else {
            callback.onError("Không thể xử lý file ảnh!");
        }
    }
}