package com.example.android_app.data.repository;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.android_app.data.local.AppDatabase;
import com.example.android_app.data.local.UserDao;
import com.example.android_app.data.local.UserEntity;
import com.example.android_app.data.network.ApiClient;
import com.example.android_app.data.network.ApiService;
import com.example.android_app.model.LoginRequest;
import com.example.android_app.model.LoginResponse;
import com.example.android_app.utils.SharedPrefsManager;
import com.example.android_app.utils.UserManager;
import com.example.android_app.utils.UserMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


//This class is responsible for interacting with the Network layer (API):
public class UserRepository {
    private final ApiService apiService; //retrofit interface
    private final Context context; //context to casting api
    private final Gson gson = new Gson();
    private final UserDao userDao;
    private final Executor executor;

    public UserRepository(Context context) {
        this.context = context.getApplicationContext();
        apiService = ApiClient.getClient().create(ApiService.class); //create object from retrofit
        userDao = AppDatabase.getInstance(context).userDao();
        executor = Executors.newSingleThreadExecutor();
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
                    int nameIndex = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
                    result = cursor.getString(nameIndex);
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


    public void login(LoginRequest request, MutableLiveData<LoginResponse> result, MutableLiveData<String> error) {
        Call<LoginResponse> call = apiService.loginUser(request);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(response.body());
                    String userId = response.body().getUserId();
                    if (userId != null) {
                        UserManager.saveUserId(context, userId);
                    }

                    UserEntity user = UserMapper.fromLoginResponse(response.body());
                    insertUser(user);
                    SharedPrefsManager.save(context, "userId", response.body().getUserId());
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        JsonObject jsonError = gson.fromJson(errorBody, JsonObject.class);
                        String msg = jsonError.has("message") ? jsonError.get("message").getAsString() : "Login failed";
                        error.postValue(msg);
                    } catch (Exception e) {
                        error.postValue("Unexpected error");
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }


    public void updateUserProfile(String userId, String newName, Uri imageUri,
                                  MutableLiveData<String> successMsg, MutableLiveData<String> errorMsg) {

        RequestBody nameBody = RequestBody.create(MediaType.parse("text/plain"), newName);

        MultipartBody.Part imagePart = null;
        if (imageUri != null) {
            File imageFile = getFileFromUri(imageUri);
            if (imageFile != null) {
                RequestBody imageRequest = RequestBody.create(MediaType.parse("image/*"), imageFile);
                imagePart = MultipartBody.Part.createFormData("profileImage", imageFile.getName(), imageRequest);
            }
        }

        Call<LoginResponse> call = apiService.updateUser(userId, nameBody, imagePart);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse body = response.body();
                    String userId = body.getUserId();
                    if (userId != null) {
                        UserManager.saveUserId(context, userId);
                    }

                    SharedPrefsManager.saveProfile(context, body.getFullName(), body.getProfileImage());

                    UserEntity updatedUser = UserMapper.fromLoginResponse(body);
                    insertUser(updatedUser);
                    successMsg.postValue("Profile updated successfully");
                } else {
                    errorMsg.postValue("Failed to update profile");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                errorMsg.postValue("Network error: " + t.getMessage());
            }
        });
    }
    public void insertUser(UserEntity user) {
        if (user == null || user._id == null || user._id.isEmpty()) {
            return;
        }
        executor.execute(() -> userDao.insertUser(user));
    }
    public void getUserById(String id, LocalCallback<UserEntity> callback) {
        executor.execute(() -> {
            UserEntity user = userDao.getUserById(id);
            callback.onResult(user);
        });
    }

    public interface LocalCallback<T> {
        void onResult(T result);
    }

}
