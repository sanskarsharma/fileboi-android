package dev.sanskar.fileboi.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import dev.sanskar.fileboi.R;
import dev.sanskar.fileboi.core.models.FileItem;
import dev.sanskar.fileboi.core.models.FileMetadata;
import dev.sanskar.fileboi.utilities.ConversionUtils;
import dev.sanskar.fileboi.utilities.DateTimeUtils;

public class FileItemAdapter extends RecyclerView.Adapter<FileItemAdapter.MyViewHolder> {

    public static final String TAG = FileItemAdapter.class.getSimpleName();

    Context mCtx;
    List<FileItem> fileItemList;
    private FileItemClickListener fileItemClickListener;

    /*
        note on adapter's role in MVVM patter :
        - viewModel should not be accessed from here. changes should be made to data (repository) which the viewmodel will observe and
        - view model will in-turn reflect the changes in repository to its LiveData - which Activity/Fragment is observing. hence changes reach Activity where we reset adapter on change
     */

    public FileItemAdapter(Context mCtx, List<FileItem> fileItemList, FileItemClickListener fileItemClickListener) {
        this.mCtx = mCtx;
        this.fileItemList = fileItemList;
        this.fileItemClickListener = fileItemClickListener;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.file_item_cardview, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        final FileItem fileItem = fileItemList.get(position);

        int placeholderDrawableResource;
        if (fileItem.isImage()) {
            placeholderDrawableResource = R.drawable.icons8_placeholder_image;
            Log.e(TAG, fileItem.getExtras().getThumbnailUrl());

        } else if (fileItem.getName().endsWith(".pdf")) {
            placeholderDrawableResource = R.drawable.icons8_placeholder_pdf;
        } else if (fileItem.isVideo()) {
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

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {

                    final int position = getAdapterPosition();

                    // inflating a popup menu on click
                    PopupMenu popupMenu = new PopupMenu(view.getContext(), view, Gravity.END);
                    popupMenu.inflate(R.menu.menu_file);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(final MenuItem menuItem) {
                            switch (menuItem.getItemId()) {

                                case R.id.menu_file_option_view : {
                                    fileItemClickListener.onViewButtonClick(fileItemList.get(position),position, view);
                                    return true;
                                }

                                case R.id.menu_file_option_download : {
                                    fileItemClickListener.onShareButtonClick(fileItemList.get(position),position, view);
                                    return true;
                                }

                                case R.id.menu_file_option_delete: {
                                    fileItemClickListener.onDeleteButtonClick(fileItemList.get(position), position, view);
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
    }
}