package dev.sanskar.fileboi;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dev.sanskar.fileboi.activities.LoginActivity;
import dev.sanskar.fileboi.adapters.FilesAdapter;
import dev.sanskar.fileboi.core.models.FileItem;
import dev.sanskar.fileboi.core.services.FileboiAPI;
import dev.sanskar.fileboi.core.schema.UploadTaskResult;
import dev.sanskar.fileboi.utilities.Constants;
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

    FilesViewModel filesViewModel;

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
                getFileIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
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
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && fabBtnUploadFile.isShown())
                    fabBtnUploadFile.hide();
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    fabBtnUploadFile.show();
                super.onScrollStateChanged(recyclerView, newState);
            }
        });


        filesViewModel = ViewModelProviders.of(this).get(FilesViewModel.class);
        filesViewModel.getLiveData().observe(this, new Observer<List<FileItem>>() {
            @Override
            public void onChanged(@Nullable List<FileItem> fileItemList) {
                if (fileItemList != null) {
                    filesAdapter = new FilesAdapter(MainActivity.this, fileItemList);
                    recyclerView.setAdapter(filesAdapter);
                }
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                filesViewModel.getLiveData();

            }
        });


        // Get intent for capturing share menu intents
        Intent incomingIntent = getIntent();
        // currently incoming intent can have any MimeType, there is no way to ensure whether it is a file or plain text
        // this can be done by using (scheme = file) or (scheme = content) attributes in intent filter
        // but the intent that is sharing the file with your app might not supply the scheme information
        // and due to this our app will not appear in the share menu of the app from which the file is being shared
        // all in all, hard to filter "only files" using intent filter currently
        // there is an open question on SO for this : https://stackoverflow.com/questions/12247957/intent-filter-for-files-only
        if ((Intent.ACTION_SEND.equals(incomingIntent.getAction()) || Intent.ACTION_SEND_MULTIPLE.equals(incomingIntent.getAction())) && incomingIntent.getType() != null) {
            handleFileSelectionIntent(incomingIntent);
        }

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


    private void processSelectedFiles(final List<Uri> uriList) {
        final Context context = this;
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser != null) {
            mUser.getIdToken(true)
                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                String idToken = task.getResult().getToken();

                                for (Uri uri : uriList) {  // TODO : instead of looping here, pass the list and make appropriate changes in UploadTask to handle it. This way, multiple files would be uploaded serially in order instead of all at once
                                    String imagePath = FileUploadUtils.getPath(context, uri);

                                    // using THREAD_POOL_EXECUTOR for running multiple parallel executions
                                    new UploadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imagePath, idToken);
                                }

                            } else {
                                // Handle error -> task.getException();
                            }
                        }
                    });
        }
    }

    private void handleFileSelectionIntent (Intent dataIntent) {
        final List<Uri> uriList = new ArrayList<>();

        // checking multiple selection or not ; ref : https://stackoverflow.com/a/48824844/7314323
        if(null != dataIntent.getClipData()) {
            if (dataIntent.getClipData().getItemCount() > Constants.FILE_SELECTION_MAX_COUNT) {
                Toast.makeText(this, "Can't share more than 10 media items in one selection", Toast.LENGTH_LONG).show();
                return;
            }
            for(int i = 0; i < dataIntent.getClipData().getItemCount(); i++) {
                uriList.add(dataIntent.getClipData().getItemAt(i).getUri());
            }
        } else {
            uriList.add(dataIntent.getData());
        }

        // ask for confirmation
        MaterialAlertDialogBuilder uploadConfirmDialog = new MaterialAlertDialogBuilder(this)
                .setMessage("Upload " +  String.valueOf(uriList.size()) +" files ?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        processSelectedFiles(uriList);
                    }
                });
        uploadConfirmDialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            handleFileSelectionIntent(data);
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




    private class UploadTask extends AsyncTask<String, Void, UploadTaskResult> {

        NotificationHelper notificationHelper = new NotificationHelper(MainActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected UploadTaskResult doInBackground(String... params) {

            int randomInt = new Random().nextInt(999999);
            String [] namesplit = params[0].split("/");
            String name = namesplit[namesplit.length-1];

            UploadTaskResult uploadTaskResult = new UploadTaskResult(randomInt, name, false);

            // starting upload notification
            NotificationCompat.Builder notification = notificationHelper.getFileUploadNotification();
            notification.setContentTitle("Uploading file");
            notification.setContentText(name);
            notification.setProgress(100, 0, true);
            notificationHelper.notify(uploadTaskResult.getNotificationId(), notification);

            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", name);

                // create entry
                Request getUrlRequest = new Request.Builder()
                        .url(FileboiAPI.FILES_URL)
                        .post(RequestBody.create(MediaType.parse("application/json"), jsonObject.toString()))
                        .header("Authorization", "Bearer " + params[1])
                        .build();

                Response getUrlResponse = HttpUtils.getHttpClient().newCall(getUrlRequest).execute();
                if (!getUrlResponse.isSuccessful()){
                    Log.e(TAG, String.valueOf(getUrlResponse.code()));
                    uploadTaskResult.setSuccess(false);
                    return uploadTaskResult;
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
                    uploadTaskResult.setSuccess(false);
                    return uploadTaskResult;
                }
                String respJsonStr = getUploadUrlResponse.body().string();
                JSONObject uploadurlresp = new JSONObject(respJsonStr);
                String uploadUrl = uploadurlresp.getString("url");

                // Upload file to s3.
                String imagePath = params[0];
                Request uploadFileRequest = new Request.Builder()
                        .url(uploadUrl)
                        .put(RequestBody.create(MediaType.parse(""), new File(imagePath)))
                        .build();
                Response uploadResponse = HttpUtils.getHttpClient().newCall(uploadFileRequest).execute();
                if (!uploadResponse.isSuccessful()){
                    Log.e(TAG, String.valueOf(uploadResponse.code()));
                    uploadTaskResult.setSuccess(false);
                    return uploadTaskResult;
                }

                uploadTaskResult.setSuccess(true);
                return uploadTaskResult;

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());

                uploadTaskResult.setSuccess(false);
                return uploadTaskResult;            }
        }

        @Override
        protected void onPostExecute(UploadTaskResult uploadTaskResult) {
            super.onPostExecute(uploadTaskResult);

            if (uploadTaskResult.isSuccess()) {
                NotificationCompat.Builder notification = notificationHelper.getFileUploadNotification();
                notification.setContentTitle("Upload complete");
                notification.setContentText(uploadTaskResult.getObjectName());
                notification.setProgress(0, 0, false);
                notificationHelper.notify(uploadTaskResult.getNotificationId(), notification);

            } else {
                Log.e(TAG, "UploadTask failed");
                NotificationCompat.Builder notification = notificationHelper.getFileUploadNotification();
                notification.setContentTitle("Upload failed");
                notification.setContentText("Failed to upload " + uploadTaskResult.getObjectName() + ".\n Please try again. Make sure internet is accessible");

                notification.setProgress(0, 0, false);
                notificationHelper.notify(uploadTaskResult.getNotificationId(), notification);
            }

            // refreshing filesViewModel
            filesViewModel.getLiveData();

        }
    }


}