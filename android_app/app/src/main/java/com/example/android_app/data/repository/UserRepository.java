package com.example.android_app.data.repository;

import android.net.Uri;
import android.content.Context;
import android.database.Cursor;
import android.provider.OpenableColumns;

import com.example.android_app.network.ApiClient;
import com.example.android_app.network.ApiService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

//This class is responsible for interacting with the Network layer (API):
public class UserRepository {
    private final ApiService apiService; //retrofit interface
    private final Context context; //context to casting api

    public UserRepository(Context context) {
        this.context = context.getApplicationContext();
        apiService = ApiClient.getClient().create(ApiService.class); //create object from retrofit
    }

    public Call<ResponseBody> registerUser(String fullName, String email, String birthDate, String gender, String password, Uri imageUri) {
        //create request body for each parameter
        RequestBody fullNameBody = RequestBody.create(MediaType.parse("text/plain"), fullName);
        RequestBody emailBody = RequestBody.create(MediaType.parse("text/plain"), email);
        RequestBody birthDateBody = RequestBody.create(MediaType.parse("text/plain"), birthDate);
        RequestBody genderBody = RequestBody.create(MediaType.parse("text/plain"), gender);
        RequestBody passwordBody = RequestBody.create(MediaType.parse("text/plain"), password);

        //create request body for image
        MultipartBody.Part imagePart = null;
        if (imageUri != null) {
            File imageFile = getFileFromUri(imageUri);
            if (imageFile != null) {
                RequestBody imageRequest = RequestBody.create(MediaType.parse("image/*"), imageFile);
                imagePart = MultipartBody.Part.createFormData("profileImage", imageFile.getName(), imageRequest);
            }
        }

        return apiService.registerUser(fullNameBody, emailBody, birthDateBody, genderBody, passwordBody, imagePart);
    }

    //convert uri to file
    //read file content and write it to a temporary file in cache so it can be sent to the server
    private File getFileFromUri(Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            String fileName = getFileName(uri);
            File file = new File(context.getCacheDir(), fileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //get file name from uri so it can be sent to the server
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }
}
