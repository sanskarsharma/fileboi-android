package dev.sanskar.fileboi.adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import dev.sanskar.fileboi.MainActivity;
import dev.sanskar.fileboi.R;
import dev.sanskar.fileboi.core.models.FileItem;
import dev.sanskar.fileboi.fragments.SlideshowDialogFragment;

public class FileItemGridAdapter extends RecyclerView.Adapter<FileItemGridAdapter.MyViewHolder>{

    public static final String TAG = FileItemGridAdapter.class.getSimpleName();
    Context mCtx;
    List<FileItem> fileItemList;

    public FileItemGridAdapter(Context mCtx, List<FileItem> fileItemList ) {
        this.mCtx = mCtx;
        this.fileItemList = fileItemList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mCtx)
                .inflate(R.layout.file_item_grid_square, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
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
                .into(holder.squareImageView);


        holder.squareImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // casting context to MainActivity to call getSupportFragmentManager()
                // fixme : can we remove reference of particular activity from here so that this adapter is not dependent on the activity which needs it ?
                MainActivity mainActivity = (MainActivity) mCtx;
                Bundle bundle = new Bundle();
                bundle.putInt("position", position);

                FragmentTransaction ft =  mainActivity.getSupportFragmentManager().beginTransaction();
                SlideshowDialogFragment newFragment = SlideshowDialogFragment.newInstance();
                newFragment.setArguments(bundle);
                newFragment.show(ft, "slideshow");

            }
        });


    }

    @Override
    public int getItemCount() {
        return fileItemList != null ? fileItemList.size() : 0 ;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView squareImageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            squareImageView = itemView.findViewById(R.id.grid_square_image_view);

        }
    }
}