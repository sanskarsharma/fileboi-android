package dev.sanskar.fileboi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.Button;
import android.widget.ProgressBar;
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
import java.util.List;

import dev.sanskar.fileboi.activities.LoginActivity;
import dev.sanskar.fileboi.adapters.FilesAdapter;
import dev.sanskar.fileboi.api.Files;
import dev.sanskar.fileboi.backend.FileboiAPI;
import dev.sanskar.fileboi.utilities.FileUploadUtils;
import dev.sanskar.fileboi.utilities.HttpUtils;
import dev.sanskar.fileboi.view_models.FilesViewModel;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    Button btnUploadFile ;
    ProgressBar progressBar;

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

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

//        btnUploadFile = (Button) findViewById(R.id.buttonUploadFile);
//        btnUploadFile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                final Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
//                galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
//                galleryIntent.setType("*/*");
//                if (mIsPermissionGranted) {
//                    startActivityForResult(galleryIntent, REQUEST_CODE);
//                } else {
//                    Toast.makeText(MainActivity.this, "WRITE_EXTERNAL_STORAGE permission was not granted.", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

        fabBtnUploadFile = (FloatingActionButton) findViewById(R.id.floatingActionButtonUploadFile);
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

        FilesViewModel model = ViewModelProviders.of(this).get(FilesViewModel.class);
        model.getFiles().observe(this, new Observer<List<Files>>() {
            @Override
            public void onChanged(@Nullable List<Files> filesList) {
                filesAdapter = new FilesAdapter(MainActivity.this, filesList);
                recyclerView.setAdapter(filesAdapter);
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

//            btnUploadFile.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setClickable(false);


            Log.e(TAG, data.toString());
            Log.e(TAG, data.getData().getPath());


            Uri selectedImageUri = data.getData();
//            final String imagePath = getPath(selectedImageUri);
//            final String imagePath = getRealPathFromDocumentUri(data.getData());
            final String imagePath = FileUploadUtils.getPath(this, selectedImageUri);

            Log.e(TAG, imagePath);

            FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
            if (mUser != null) {
                mUser.getIdToken(true)
                        .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                            public void onComplete(@NonNull Task<GetTokenResult> task) {
                                if (task.isSuccessful()) {
                                    String idToken = task.getResult().getToken();
                                    // Send token to your backend via HTTPS
                                    // ...
                                    Log.d("TOKEN", idToken);
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
        // R.menu.mymenu is a reference to an xml file named mymenu.xml which should be inside your res/menu directory.
        // If you don't have res/menu, just create a directory named "menu" inside res
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




    private class UploadTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            Log.e(TAG, params[0]);
            Log.e(TAG, params[0]);


            try {

                JSONObject jsonObject = new JSONObject();
                String [] namesplit = params[0].split("/");
                String name = namesplit[namesplit.length-1];
                jsonObject.put("name", name);
                Log.e(TAG, jsonObject.toString());

                // create entry
                Request getUrlRequest = new Request.Builder()
                        .url(FileboiAPI.FILES_URL)
                        .post(RequestBody.create(MediaType.parse("application/json"), jsonObject.toString()))
                        .header("Authorization", "Bearer " + params[1])
                        .build();

                Response getUrlResponse = HttpUtils.getHttpClient().newCall(getUrlRequest).execute();
                if (!getUrlResponse.isSuccessful()){
                    Log.e(TAG, String.valueOf(getUrlResponse.code()));
                    return Boolean.FALSE;
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
                    return Boolean.FALSE;
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
                    return Boolean.FALSE;
                }

                return Boolean.TRUE;

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                return Boolean.FALSE;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (!result) {
                Log.e(TAG, "UploadTask failed");
                Toast.makeText(getApplicationContext(), "UploadTask finished with error", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "UploadTask finished successfully", Toast.LENGTH_LONG).show();
            }

            progressBar.setVisibility(View.INVISIBLE);
//            btnUploadFile.setVisibility(View.VISIBLE);
            recyclerView.setClickable(true);

        }
    }


}