package dev.sanskar.fileboi.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.List;

import dev.sanskar.fileboi.R;
import dev.sanskar.fileboi.adapters.FileItemViewPagerAdapter;
import dev.sanskar.fileboi.core.models.FileItem;
import dev.sanskar.fileboi.utilities.layout.ViewPagerFixed;
import dev.sanskar.fileboi.view_models.FileItemViewModel;

public class SlideshowDialogFragment extends DialogFragment {
    private String TAG = SlideshowDialogFragment.class.getSimpleName();

    private ViewPagerFixed viewPagerFixed;
    private TextView lblCount, lblTitle, lblDate;
    private int selectedPosition = 0;

    private FileItemViewModel fileItemViewModel;
    private List<FileItem> fileItems;

    public static SlideshowDialogFragment newInstance() {
        SlideshowDialogFragment f = new SlideshowDialogFragment();
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fileItemViewModel = ViewModelProviders.of(this).get(FileItemViewModel.class);

        // get data list from ViewModel and observe for changes
        fileItems = fileItemViewModel.getFileItems().getValue();
        fileItemViewModel.getFileItems().observe(this, new Observer<List<FileItem>>() {
            @Override
            public void onChanged(@Nullable List<FileItem> fileItemList) {
                if (fileItemList != null) {
                    fileItems = fileItemList;
                }

            }
        });
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_slideshow_dialog, container, false);
        viewPagerFixed = (ViewPagerFixed) v.findViewById(R.id.viewpager);
        lblCount = (TextView) v.findViewById(R.id.lbl_count);
        lblTitle = (TextView) v.findViewById(R.id.title);
        lblDate = (TextView) v.findViewById(R.id.date);

        selectedPosition = getArguments().getInt("position");

        Log.e(TAG, "position: " + selectedPosition);

        FileItemViewPagerAdapter fileItemViewPagerAdapter = new FileItemViewPagerAdapter(getContext(), fileItems);
        viewPagerFixed.setAdapter(fileItemViewPagerAdapter);
        viewPagerFixed.addOnPageChangeListener(viewPagerPageChangeListener);

        setCurrentItem(selectedPosition);

        return v;
    }

    private void setCurrentItem(int position) {
        viewPagerFixed.setCurrentItem(position, false);
        displayMetaInfo(selectedPosition);
    }

    ViewPagerFixed.OnPageChangeListener viewPagerPageChangeListener = new ViewPagerFixed.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            displayMetaInfo(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private void displayMetaInfo(int position) {
        lblCount.setText((position + 1) + " of " + fileItems.size());
        FileItem fileItem = fileItems.get(position);
        lblTitle.setText(fileItem.getName());
        lblDate.setText(fileItem.getCreatedAt());
    }

}
