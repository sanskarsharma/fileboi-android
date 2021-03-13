package dev.sanskar.fileboi.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.List;

import dev.sanskar.fileboi.R;
import dev.sanskar.fileboi.adapters.FileItemViewPagerAdapter;
import dev.sanskar.fileboi.core.models.FileItem;
import dev.sanskar.fileboi.repositories.FileItemRepository;
import dev.sanskar.fileboi.utilities.layout.ViewPagerFixed;
import dev.sanskar.fileboi.view_models.FileItemViewModel;

public class SlideshowDialogFragment extends DialogFragment {
    private String TAG = SlideshowDialogFragment.class.getSimpleName();

    private ViewPagerFixed viewPagerFixed;
    private TextView lblCount, lblTitle, lblDate;
    private ImageButton imageButtonShare ;
    private int selectedPosition = 0;

    private FileItemViewModel fileItemViewModel;
    private FileItemRepository fileItemRepository = FileItemRepository.getInstance();

    private FileItemViewPagerAdapter fileItemViewPagerAdapter;
    private List<FileItem> fileItems;

    public static SlideshowDialogFragment newInstance() {
        SlideshowDialogFragment f = new SlideshowDialogFragment();
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fileItemViewModel = ViewModelProviders.of(requireActivity()).get(FileItemViewModel.class);

        // get data list from ViewModel, attach to adapter, and observe for changes
        fileItems = fileItemViewModel.getFileItems().getValue();
        fileItemViewPagerAdapter = new FileItemViewPagerAdapter(getContext(), fileItems);

        fileItemViewModel.getFileItems().observe(this, new Observer<List<FileItem>>() {
            @Override
            public void onChanged(@Nullable List<FileItem> fileItemList) {

                // onChanged is call everytime viewpager is opened ; not an issue here, but good to note when adding more logic inside it
                // read : https://blog.usejournal.com/observe-livedata-from-viewmodel-in-fragment-fd7d14f9f5fb

                if (fileItemList != null) {
                    fileItems = fileItemList;
                    fileItemViewPagerAdapter.notifyDataSetChanged();
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

        imageButtonShare = (ImageButton) v.findViewById(R.id.imageButtonShare);
        imageButtonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileItem fileItem = fileItems.get(selectedPosition);
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Here is a file for you  : " + fileItem.getExtras().getDownloadUrl());
                sendIntent.setType("text/plain");
                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);
            }
        });

        viewPagerFixed.setAdapter(fileItemViewPagerAdapter);
        viewPagerFixed.addOnPageChangeListener(viewPagerPageChangeListener);

        selectedPosition = getArguments().getInt("position");
        viewPagerFixed.setCurrentItem(selectedPosition, false);
        displayMetaInfo(selectedPosition);

        return v;
    }

    ViewPagerFixed.OnPageChangeListener viewPagerPageChangeListener = new ViewPagerFixed.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            selectedPosition = position;
            displayMetaInfo(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private void displayMetaInfo(int position) {
        // lblCount.setText((position + 1) + " of " + fileItems.size());
        FileItem fileItem = fileItems.get(position);
        lblTitle.setText(fileItem.getName());
    }

}
