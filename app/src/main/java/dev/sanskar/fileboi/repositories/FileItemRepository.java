package dev.sanskar.fileboi.repositories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import dev.sanskar.fileboi.Fileboi;
import dev.sanskar.fileboi.core.models.FileItem;
import dev.sanskar.fileboi.core.services.FilesAPIService;
import dev.sanskar.fileboi.utilities.HttpUtils;
import dev.sanskar.fileboi.utilities.SharedPrefHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FileItemRepository {

    private static FileItemRepository instance;
    private MutableLiveData<List<FileItem>> mutableLiveData = new MutableLiveData<>();
    private FilesAPIService filesAPIService = HttpUtils.getRetrofitInstance(FilesAPIService.SERVICE_BASE_URL).create(FilesAPIService.class);

    static Gson gson = new Gson();

    public static FileItemRepository getInstance() {
        if (instance == null) {
            synchronized (FileItemRepository.class) {
                if (instance == null) {
                    instance = new FileItemRepository();
                }
            }
        }
        return instance;
    }

    // The getter upcasts to LiveData, this ensures that only the repository can cause a change
    public LiveData<List<FileItem>> getFileItems() {
        return mutableLiveData;
    }

    public void triggerRefresh() {

        // loading data from saved preferences
        Context context = Fileboi.getContext();
        if (context != null){
            String data = SharedPrefHelper.getStringData(context, SharedPrefHelper.KEY_GET_FILES_API_RESPONSE_BODY);
            if (data != null){
                List<FileItem> fileItems = gson.fromJson(data, new TypeToken<List<FileItem>>(){}.getType());
                if (fileItems != null && fileItems.size() > 0){
                    mutableLiveData.postValue(fileItems);
                }
            }
        }

        callLoadFiles();
    }

    public void deleteFileItem(FileItem fileItem) {
        // todo : implement or remove
        // logic to make delete file api call and then update liveData (either re-fetch files OR delete entry from liveData)
//        List<FileItem> fileItems = mutableLiveData.getValue();
//        fileItems.remove(fileItem);
//        mutableLiveData.postValue(fileItems);
    }

    public void createFileItem(FileItem fileItem) {
        // todo : implement or remove
    }

    public void getFileItemDownloadURL(FileItem fileItem) {
        // todo : implement or remove
    }

    private void callLoadFiles() {
        //we will load it asynchronously from server in this method
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser != null) {
            mUser.getIdToken(true)
                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                String idToken = task.getResult().getToken();
                                loadFiles(idToken);

                            } else {
                                // Handle error -> task.getException();
                                // TODO : handle this case gracefully

                            }
                        }
                    });
        }
    }

    // This method is using retrofit to get the JSON data from our web service
    private void loadFiles(String token) {

        Call<List<FileItem>> callGetFiles = filesAPIService.getFiles("Bearer " + token);

        // using enqueue (async callback) instead of execute() as this throws NetworkOnMainThreadException then
        // probably the caller (firebase callback) does not call this truly async
        callGetFiles.enqueue(new Callback<List<FileItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<FileItem>> call, @NonNull Response<List<FileItem>> response) {
               if (response.body() != null) {
                   mutableLiveData.postValue(response.body());
                   SharedPrefHelper.saveData(
                           Fileboi.getContext(),
                           SharedPrefHelper.KEY_GET_FILES_API_RESPONSE_BODY,
                           gson.toJson(response.body())
                   );
               }

            }

            @Override
            public void onFailure(@NonNull Call<List<FileItem>> call, @NonNull Throwable t) {
                // TODO : handle this case gracefully
                t.printStackTrace();
            }
        });

    }

}
