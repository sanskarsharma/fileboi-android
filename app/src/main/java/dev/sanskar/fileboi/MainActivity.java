package dev.sanskar.fileboi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONObject;

import java.io.File;
import java.util.Collections;
import java.util.List;

import dev.sanskar.fileboi.activities.LoginActivity;
import dev.sanskar.fileboi.adapters.FilesAdapter;
import dev.sanskar.fileboi.api.Files;
import dev.sanskar.fileboi.backend.FileboiAPI;
import dev.sanskar.fileboi.utilities.FileUploadUtils;
import dev.sanskar.fileboi.utilities.HttpUtils;
import dev.sanskar.fileboi.utilities.notif.NotificationHelper;
import dev.sanskar.fileboi.view_models.FilesViewModel;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    SwipeRefreshLayout swipeRefreshLayout;
    FloatingActionButton fabBtnUploadFile;
    RecyclerView recyclerView;
    FilesAdapter filesAdapter;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE = 1111;
    private boolean mIsPermissionGranted = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        fabBtnUploadFile = findViewById(R.id.floatingActionButtonUploadFile);
        fabBtnUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent getFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                getFileIntent.setType("*/*");
                if (mIsPermissionGranted) {
                    startActivityForResult(getFileIntent, REQUEST_CODE);
                } else {
                    Toast.makeText(MainActivity.this, "WRITE_EXTERNAL_STORAGE permission was not granted.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        final FilesViewModel filesViewModel = ViewModelProviders.of(this).get(FilesViewModel.class);
        filesViewModel.getFiles().observe(this, new Observer<List<Files>>() {
            @Override
            public void onChanged(@Nullable List<Files> filesList) {
                if (filesList != null) {
                    // TODO : reversing on client for now ; fix this properly when server supports ordering
                    Collections.reverse(filesList);
                }
                filesAdapter = new FilesAdapter(MainActivity.this, filesList);
                recyclerView.setAdapter(filesAdapter);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                filesViewModel.getFiles();

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (isGranted) {
            Log.i(TAG, "Permission: " + permissions[0] + " was " + grantResults[0]);
        } else {
            Log.w(TAG, "User didn't grant WRITE_EXTERNAL_STORAGE permission.");
            mIsPermissionGranted = false;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {

            Uri selectedImageUri = data.getData();
            final String imagePath = FileUploadUtils.getPath(this, selectedImageUri);

            FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
            if (mUser != null) {
                mUser.getIdToken(true)
                        .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                            public void onComplete(@NonNull Task<GetTokenResult> task) {
                                if (task.isSuccessful()) {
                                    String idToken = task.getResult().getToken();
                                    new UploadTask().execute(imagePath, idToken);

                                } else {
                                    // Handle error -> task.getException();
                                }
                            }
                        });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            AuthUI.getInstance()
                    .signOut(getApplicationContext())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            // user is now signed out
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        }
                    });
        }
        return super.onOptionsItemSelected(item);
    }




    private class UploadTask extends AsyncTask<String, Void, String> {

        NotificationHelper notificationHelper;
        private final static int NOTIFICATION_ID = 1111;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            notificationHelper = new NotificationHelper(MainActivity.this);
            NotificationCompat.Builder notification = notificationHelper.getFileUploadNotification();
            notification.setContentTitle("Starting upload");

            notificationHelper.notify(NOTIFICATION_ID, notification);
        }

        @Override
        protected String doInBackground(String... params) {

            try {

                JSONObject jsonObject = new JSONObject();
                String [] namesplit = params[0].split("/");
                String name = namesplit[namesplit.length-1];
                jsonObject.put("name", name);

                NotificationCompat.Builder notification = notificationHelper.getFileUploadNotification();
                notification.setContentTitle("Uploading file");
                notification.setContentText(name);
                notification.setProgress(100, 0, true);
                notificationHelper.notify(NOTIFICATION_ID, notification);


                // create entry
                Request getUrlRequest = new Request.Builder()
                        .url(FileboiAPI.FILES_URL)
                        .post(RequestBody.create(MediaType.parse("application/json"), jsonObject.toString()))
                        .header("Authorization", "Bearer " + params[1])
                        .build();

                Response getUrlResponse = HttpUtils.getHttpClient().newCall(getUrlRequest).execute();
                if (!getUrlResponse.isSuccessful()){
                    Log.e(TAG, String.valueOf(getUrlResponse.code()));
                    return null;
                }
                String responseJsonString = getUrlResponse.body().string();
                JSONObject getUrlResponseJson = new JSONObject(responseJsonString);
                String fileId = getUrlResponseJson.getString("id");


                // get upload url of entry
                Request getUploadUrlRequest = new Request.Builder()
                        .url(FileboiAPI.getFileUploadURL(fileId))
                        .get()
                        .header("Authorization", "Bearer " + params[1])
                        .build();
                Response getUploadUrlResponse = HttpUtils.getHttpClient().newCall(getUploadUrlRequest).execute();
                if (!getUploadUrlResponse.isSuccessful()){
                    Log.e(TAG, String.valueOf(getUploadUrlResponse.code()));
                    return null;
                }
                String respJsonStr = getUploadUrlResponse.body().string();
                JSONObject uploadurlresp = new JSONObject(respJsonStr);
                String uploadUrl = uploadurlresp.getString("url");


                // Upload file to Amazon.
                String imagePath = params[0];
                Request uploadFileRequest = new Request.Builder()
                        .url(uploadUrl)
                        .put(RequestBody.create(MediaType.parse(""), new File(imagePath)))
                        .build();
                Response uploadResponse = HttpUtils.getHttpClient().newCall(uploadFileRequest).execute();
                if (!uploadResponse.isSuccessful()){
                    Log.e(TAG, String.valueOf(uploadResponse.code()));
                    return null;
                }

                return name;

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                Log.e(TAG, "UploadTask failed");
                NotificationCompat.Builder notification = notificationHelper.getFileUploadNotification();
                notification.setContentTitle("Upload failed");
                notification.setProgress(0, 0, false);
                notificationHelper.notify(NOTIFICATION_ID, notification);

            } else {
                NotificationCompat.Builder notification = notificationHelper.getFileUploadNotification();
                notification.setContentTitle("Upload complete");
                notification.setContentText(result);
                notification.setProgress(0, 0, false);
                notificationHelper.notify(NOTIFICATION_ID, notification);

            }

        }
    }


}