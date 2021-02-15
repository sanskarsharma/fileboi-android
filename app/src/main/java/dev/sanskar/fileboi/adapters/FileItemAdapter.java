package dev.sanskar.fileboi.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.util.List;

import dev.sanskar.fileboi.R;
import dev.sanskar.fileboi.core.models.FileItem;
import dev.sanskar.fileboi.core.models.FileMetadata;
import dev.sanskar.fileboi.core.schema.FileURLResponse;
import dev.sanskar.fileboi.core.services.FilesAPIService;
import dev.sanskar.fileboi.repositories.FileItemRepository;
import dev.sanskar.fileboi.utilities.ConversionUtils;
import dev.sanskar.fileboi.utilities.DateTimeUtils;
import dev.sanskar.fileboi.utilities.HttpUtils;

public class FileItemAdapter extends RecyclerView.Adapter<FileItemAdapter.MyViewHolder> {

    public static final String TAG = FileItemAdapter.class.getSimpleName();
    private FilesAPIService filesAPIService = HttpUtils.getRetrofitInstance(FilesAPIService.SERVICE_BASE_URL).create(FilesAPIService.class);
    // todo : think of way to remove dependence of adapter on filesAPIService

    Context mCtx;
    List<FileItem> fileItemList;

    /*
        note on adapter's role in MVVM patter :
        - viewModel should not be accessed from here. changes should be made to data (repository) which the viewmodel will observe and
        - view model will in-turn reflect the changes in repository to its LiveData - which Activity/Fragment is observing. hence changes reach Activity where we reset adapter on change
     */

    public FileItemAdapter(Context mCtx, List<FileItem> fileItemList) {
        this.mCtx = mCtx;
        this.fileItemList = fileItemList;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.files_entry_cardview_new, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        final FileItem fileItem = fileItemList.get(position);

        int placeholderDrawableResource;
        if (fileItem.getName().endsWith(".jpeg") || fileItem.getName().endsWith(".jpg") || fileItem.getName().endsWith(".png") || fileItem.getName().endsWith(".gif")) {
            placeholderDrawableResource = R.drawable.icons8_placeholder_image;
            Log.e(TAG, fileItem.getExtras().getThumbnailUrl());

        } else if (fileItem.getName().endsWith(".pdf")) {
            placeholderDrawableResource = R.drawable.icons8_placeholder_pdf;
        } else if (fileItem.getName().endsWith(".mp4") || (fileItem.getName().endsWith(".3gp"))) {
            placeholderDrawableResource = R.drawable.icons8_placeholder_video;
        } else {
            placeholderDrawableResource = R.drawable.icons8_placeholder_file;
        }

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(placeholderDrawableResource)
                .error(placeholderDrawableResource)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);

        Glide.with(mCtx)
                .load(fileItem.getExtras().getThumbnailUrl())
                .apply(requestOptions)
                .into(holder.imageView);

        String itemName = fileItem.getName();
        holder.itemNameTextView.setText(itemName);

        FileMetadata fileMetadata = fileItem.getFileMetadata();
        if (fileMetadata != null && fileMetadata.getSizeInBytes() > 0) {
            holder.shortInfoTextView.setText(ConversionUtils.getReadableSize(fileMetadata.getSizeInBytes()));
            holder.shortInfoTextView.setTextColor(ContextCompat.getColor(mCtx, R.color.cardview_item_name_text_color));
        } else {
            holder.shortInfoTextView.setText("(" + "Upload pending"+ ")");
            holder.shortInfoTextView.setTextColor(Color.RED);
        }
        // always make sure of adding code in else condition when writing inside onBindViewHolder() for any adapter
        // viewholders are re-used for elements of list and might mess up the UI if else condition is not explicitly specified
        // ref : https://stackoverflow.com/a/54819411/7314323
        // also FIXME : this color setting via code is a temporary work-around for showing status of file upload
        // Ideally, this should be replaced by better UI with icons etc for showing status etc
        // Nevertheless, that might also need some if/else's - so this should be noted there too.

        try {
            String[] arr = DateTimeUtils.getFormattedDateTimeString(fileItem.getCreatedAt()).split("-");
            String entryDate= arr[0];
            String entryTime= arr[1];
            holder.dateTimeTextView.setText(entryTime + ", " + entryDate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // inflating a popup menu on click
                PopupMenu popupMenu = new PopupMenu(view.getContext(), view, Gravity.END);
                popupMenu.inflate(R.menu.menu_file);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(final MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.menu_file_option_download : {
                                Toast.makeText(mCtx, "Downloading \n" + fileItem.getName(), Toast.LENGTH_SHORT).show();

                                FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (mUser != null) {
                                    mUser.getIdToken(true)
                                            .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                                public void onComplete(@NonNull Task<GetTokenResult> task) {
                                                    if (task.isSuccessful()) {
                                                        String idToken = task.getResult().getToken();

                                                        retrofit2.Call<FileURLResponse> getFileDownloadUrl = filesAPIService.getFileDownloadUrl("Bearer " + idToken, fileItem.getId());
                                                        getFileDownloadUrl.enqueue(new retrofit2.Callback<FileURLResponse>() {
                                                            @Override
                                                            public void onResponse(retrofit2.Call<FileURLResponse> call, retrofit2.Response<FileURLResponse> response) {
                                                                // open download URL in browser
                                                                FileURLResponse fileURLResponse = response.body();
                                                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileURLResponse.getUrl()));
                                                                mCtx.startActivity(browserIntent);
                                                            }

                                                            @Override
                                                            public void onFailure(retrofit2.Call<FileURLResponse> call, Throwable t) {

                                                            }
                                                        });

                                                    } else {
                                                        // failed to fetch token from firebase
                                                        // Handle error -> task.getException();
                                                    }
                                                }
                                            });
                                }
                                return true;


                            }

                            case R.id.menu_file_option_delete: {

                                MaterialAlertDialogBuilder deleteConfirmDialog = new MaterialAlertDialogBuilder(mCtx)
                                        .setTitle(fileItem.getName())
                                        .setMessage("Are you sure you want to delete this file from cloud storage ?")
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        })
                                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                                                if (mUser != null) {
                                                    mUser.getIdToken(true)
                                                            .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                                                public void onComplete(@NonNull Task<GetTokenResult> task) {
                                                                    if (task.isSuccessful()) {
                                                                        String idToken = task.getResult().getToken();

                                                                        retrofit2.Call<FileItem> deleteFile = filesAPIService.deleteFile("Bearer " + idToken, fileItem.getId());
                                                                        deleteFile.enqueue(new retrofit2.Callback<FileItem>() {
                                                                            @Override
                                                                            public void onResponse(retrofit2.Call<FileItem> call, retrofit2.Response<FileItem> response) {

                                                                                // show toast message for file deleted
                                                                                final FileItem deletedFileItem = response.body();
                                                                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        Toast.makeText(mCtx, "Deleted \n" + deletedFileItem.getName(), Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                });

                                                                                // refresh list
                                                                                FileItemRepository.getInstance().triggerRefresh();
                                                                            }

                                                                            @Override
                                                                            public void onFailure(retrofit2.Call<FileItem> call, Throwable t) {
                                                                                // failed to delete file, api call failed
                                                                            }
                                                                        });

                                                                    } else {
                                                                        // failed to fetch token from firebase
                                                                        // Handle error -> task.getException();
                                                                    }
                                                                }
                                                            });
                                                }

                                            }
                                        })
                                        ;
                                deleteConfirmDialog.show();
                                return true;

                            }
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();

            }
        });


    }

    @Override
    public int getItemCount() {
        return fileItemList != null ? fileItemList.size() : 0 ;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView dateTimeTextView, itemNameTextView, shortInfoTextView;
        ImageView imageView;

        public MyViewHolder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.item_cardView_new);
            dateTimeTextView = itemView.findViewById(R.id.item_textView_datetime);
            itemNameTextView = itemView.findViewById(R.id.item_textView_title);
            shortInfoTextView = itemView.findViewById(R.id.item_textView_shortDescription);
            imageView = itemView.findViewById(R.id.item_imageView);

        }
    }
}