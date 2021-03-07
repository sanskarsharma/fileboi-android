package dev.sanskar.fileboi.adapters;

import android.view.View;

import dev.sanskar.fileboi.core.models.FileItem;

public interface FileItemClickListener {

    void onViewButtonClick(FileItem selectedItem, int position, View view);
    void onShareButtonClick(FileItem selectedItem, int position, View view);
    void onDeleteButtonClick(FileItem selectedItem, int position, View view);

}
