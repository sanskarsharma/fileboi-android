package dev.sanskar.fileboi.view_models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import dev.sanskar.fileboi.core.models.FileItem;
import dev.sanskar.fileboi.repositories.FileItemRepository;


public class FileItemViewModel extends ViewModel {

    // create instance of repository
    private FileItemRepository fileItemRepository = FileItemRepository.getInstance();

    // this is the data that we will fetch asynchronously and observe for change from activity/fragment
    private LiveData<List<FileItem>> fileItems ;

    public FileItemViewModel() {
        // reference repository's livedata on init
        fileItems = fileItemRepository.getFileItems();
    }

    // we will call this method to get the data
    public LiveData<List<FileItem>> getFileItems() {
        return fileItems;
    }

}
