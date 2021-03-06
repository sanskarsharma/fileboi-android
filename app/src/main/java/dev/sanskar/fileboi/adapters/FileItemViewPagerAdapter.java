package dev.sanskar.fileboi.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;

import dev.sanskar.fileboi.R;
import dev.sanskar.fileboi.core.models.FileItem;

public class FileItemViewPagerAdapter extends PagerAdapter {

    private String TAG = this.getClass().getSimpleName();

    Context mCtx;
    List<FileItem> fileItems;

    public FileItemViewPagerAdapter(Context mCtx, List<FileItem> fileItems) {
        this.mCtx = mCtx;
        this.fileItems = fileItems;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.file_item_fullscreen_preview, container, false);

        PhotoView photoViewPreview = (PhotoView) view.findViewById(R.id.image_preview);

        FileItem fileItem = fileItems.get(position);
        Log.d(TAG, "instantiated : " + fileItem.getName() + "for position" + String.valueOf(position));

        if (fileItem.isImage()) {
            RequestOptions requestOptions = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH);
            Glide.with(mCtx)
                    .load(fileItem.getExtras().getDownloadUrl())
                    .apply(requestOptions)
                    .into(photoViewPreview);
        }

        container.addView(view);

        return view;
    }

    @Override
    public int getCount() {
        return fileItems != null ? fileItems.size() : 0;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == ((View) object);
    }
}
