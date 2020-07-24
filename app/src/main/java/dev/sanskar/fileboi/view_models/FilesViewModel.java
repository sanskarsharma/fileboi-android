package dev.sanskar.fileboi.view_models;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dev.sanskar.fileboi.api.Files;
import dev.sanskar.fileboi.backend.FileboiAPI;
import dev.sanskar.fileboi.utilities.HttpUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class FilesViewModel extends ViewModel {


    //this is the data that we will fetch asynchronously
    private MutableLiveData<List<Files>> fileList;

    //we will call this method to get the data
    public LiveData<List<Files>> getFiles() {
        //if the list is null
        if (fileList == null) {
            fileList = new MutableLiveData<List<Files>>();
            //we will load it asynchronously from server in this method

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
                                    loadFiles(idToken);

                                } else {
                                    // Handle error -> task.getException();
                                }
                            }
                        });
            }
//            loadFileboiFiles();
        }

        //finally we will return the list
        return fileList;
    }


    //This method is using Retrofit to get the JSON data from URL
    private void loadFiles(String token) {
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(FileboiFileInterface.BASE_URL)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        FileboiFileInterface api = retrofit.create(FileboiFileInterface.class);
//        Call<List<Files>> call = api.getFileboiFiles();
//
//
//        call.enqueue(new Callback<List<Files>>() {
//            @Override
//            public void onResponse(Call<List<Files>> call, Response<List<Files>> response) {
//
//                //finally we are setting the list to our MutableLiveData
//                fileboiFileList.setValue(response.body());
//            }
//
//            @Override
//            public void onFailure(Call<List<Files>> call, Throwable t) {
//
//            }
//        });


        // get entries
        Request getUrlRequest = new Request.Builder()
                .url(FileboiAPI.FILES_URL)
                .get()
                .header("Authorization", "Bearer " + token)
                .build();
        Response getUrlResponse = null;

        HttpUtils.getHttpClient().newCall(getUrlRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responseJsonString = response.body().string();
                JSONArray getUrlResponseJson = null;
                try {
                    getUrlResponseJson = new JSONArray(responseJsonString);
                    List<Files> filebois= new ArrayList<Files>();
                    for (int i = 0; i < getUrlResponseJson.length(); i++) {
                        JSONObject each = getUrlResponseJson.getJSONObject(i);
                        filebois.add(new Files(each.getString("id"), each.getString("name"), each.getString("created_at")));
                    }
                    fileList.postValue(filebois);

                } catch (JSONException e) {
                    e.printStackTrace();
                }



            }
        });




    }
}
