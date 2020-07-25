package dev.sanskar.fileboi.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import dev.sanskar.fileboi.MainActivity;
import dev.sanskar.fileboi.R;
import dev.sanskar.fileboi.api.Files;
import dev.sanskar.fileboi.backend.FileboiAPI;
import dev.sanskar.fileboi.utilities.DateTimeUtils;
import dev.sanskar.fileboi.utilities.HttpUtils;
import dev.sanskar.fileboi.view_models.FilesViewModel;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FilesViewHolder> {

    Context mCtx;
    List<Files> filesList;

    // adding filesViewModel here so that we can call its methods for refreshing data list on operations like delete
    FilesViewModel filesViewModel ;

    public FilesAdapter(Context mCtx, List<Files> filesList) {
        this.mCtx = mCtx;
        this.filesList = filesList;
        this.filesViewModel = ViewModelProviders.of((MainActivity) mCtx).get(FilesViewModel.class);
    }


    @NonNull
    @Override
    public FilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.files_entry_cardview, parent, false);
        return new FilesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FilesViewHolder holder, int position) {

        final Files files = filesList.get(position);

//        Glide.with(mCtx)
//                .load(hero.getImageurl())
//                .into(holder.imageView);

        holder.itemNameTextView.setText(files.getName());
        try {
            String[] arr = DateTimeUtils.getFormattedDateTimeString(files.getCreatedAt()).split("-");
            String entryDate= arr[0];
            String entryTime= arr[1];
            holder.dayDateTextView.setText(entryDate);
            holder.timeTextView.setText(entryTime);
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
                                Toast.makeText(mCtx, "Downloading \n" + files.getName(), Toast.LENGTH_SHORT).show();

                                FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (mUser != null) {
                                    mUser.getIdToken(true)
                                            .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                                public void onComplete(@NonNull Task<GetTokenResult> task) {
                                                    if (task.isSuccessful()) {
                                                        String idToken = task.getResult().getToken();

                                                        // get entries
                                                        Request getDownloadUrlRequest = new Request.Builder()
                                                                .url(FileboiAPI.getFileDownloadURL(files.getId()))
                                                                .get()
                                                                .header("Authorization", "Bearer " + idToken)
                                                                .build();
                                                        HttpUtils.getHttpClient().newCall(getDownloadUrlRequest).enqueue(new Callback() {
                                                            @Override
                                                            public void onFailure(Call call, IOException e) {

                                                            }

                                                            @Override
                                                            public void onResponse(Call call, Response response) throws IOException {
                                                                String respJsonStr = response.body().string();
                                                                JSONObject uploadurlresp = null;
                                                                try {
                                                                    uploadurlresp = new JSONObject(respJsonStr);
                                                                    String download_url = uploadurlresp.getString("url");
                                                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(download_url));
                                                                    mCtx.startActivity(browserIntent);


                                                                } catch (JSONException e) {
                                                                    e.printStackTrace();
                                                                }


                                                            }
                                                        });


                                                    } else {
                                                        // Handle error -> task.getException();
                                                    }
                                                }
                                            });
                                }
                                return true;


                            }

                            case R.id.menu_file_option_delete: {

                                MaterialAlertDialogBuilder deleteConfirmDialog = new MaterialAlertDialogBuilder(mCtx)
                                        .setTitle(files.getName())
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

                                                                        // get entries
                                                                        Request deleteFileRequest = new Request.Builder()
                                                                                .url(FileboiAPI.getFileResourceURL(files.getId()))
                                                                                .delete()
                                                                                .header("Authorization", "Bearer " + idToken)
                                                                                .build();
                                                                        HttpUtils.getHttpClient().newCall(deleteFileRequest).enqueue(new Callback() {
                                                                            @Override
                                                                            public void onFailure(Call call, IOException e) {

                                                                            }

                                                                            @Override
                                                                            public void onResponse(Call call, Response response) throws IOException {
                                                                                String respJsonStr = response.body().string();
                                                                                JSONObject apiCallResponse = null;
                                                                                try {
                                                                                    apiCallResponse = new JSONObject(respJsonStr);
                                                                                    final String name = apiCallResponse.getString("name");

                                                                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                                                        @Override
                                                                                        public void run() {
                                                                                            Toast.makeText(mCtx, "Deleted \n" + name, Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    });

                                                                                    // refreshing filesViewModel
                                                                                    filesViewModel.getFiles();


                                                                                } catch (JSONException e) {
                                                                                    e.printStackTrace();
                                                                                }


                                                                            }
                                                                        });


                                                                    } else {
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
        return filesList.size();
    }

    class FilesViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView dayDateTextView, timeTextView, itemNameTextView;

        public FilesViewHolder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.files_cardview);
            dayDateTextView = itemView.findViewById(R.id.files_cardview_day_date_tv);
            timeTextView = itemView.findViewById(R.id.files_cardview_time_tv);
            itemNameTextView = itemView.findViewById(R.id.files_cardview_item_name_tv);

        }
    }
}