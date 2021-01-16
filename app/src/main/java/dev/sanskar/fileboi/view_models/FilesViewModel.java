package dev.sanskar.fileboi.view_models;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.util.List;

import dev.sanskar.fileboi.backend.FilesApiInterface;
import dev.sanskar.fileboi.models.FileEntry;
import dev.sanskar.fileboi.utilities.HttpUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FilesViewModel extends ViewModel {

    public static final String TAG = FilesViewModel.class.getSimpleName();

    // this is the data that we will fetch asynchronously and observe for change from activity/fragment
    private MutableLiveData<List<FileEntry>> fileList = new MutableLiveData<>();;

    // we will call this method to get the data
    public LiveData<List<FileEntry>> getFiles() {

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

        // returning the list. when above async task finishes, it will post value to this LiveData list and the observer (from activity/fragment) will be notified
        return fileList;
    }


    // This method is using retrofit to get the JSON data from our web service
    private void loadFiles(String token) {

        FilesApiInterface filesApiInterface = HttpUtils.getRetrofitInstance(FilesApiInterface.SERVICE_BASE_URL).create(FilesApiInterface.class);
        Call<List<FileEntry>> callGetFiles = filesApiInterface.getFiles("Bearer " + token);

        // using enqueue (async callback) instead of execute() as this throws NetworkOnMainThreadException then
        // probably the caller (firebase callback) does not call this truly async
        callGetFiles.enqueue(new Callback<List<FileEntry>>() {
            @Override
            public void onResponse(@NonNull Call<List<FileEntry>> call, @NonNull Response<List<FileEntry>> response) {
                fileList.postValue(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<List<FileEntry>> call, @NonNull Throwable t) {
                // TODO : handle this case gracefully
                t.printStackTrace();
            }
        });

    }
}
