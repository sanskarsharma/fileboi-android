package dev.sanskar.fileboi.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import dev.sanskar.fileboi.R;
import dev.sanskar.fileboi.api.Files;
import dev.sanskar.fileboi.backend.FileboiAPI;
import dev.sanskar.fileboi.utilities.DateTimeUtils;
import dev.sanskar.fileboi.utilities.HttpUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FilesViewHolder> {

    Context mCtx;
    List<Files> filesList;

    public FilesAdapter(Context mCtx, List<Files> filesList) {
        this.mCtx = mCtx;
        this.filesList = filesList;
    }


    @NonNull
    @Override
    public FilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.files_entry_cardview, parent, false);
        return new FilesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilesViewHolder holder, int position) {
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
                Toast.makeText(view.getContext(), files.getName(), Toast.LENGTH_SHORT).show();



                FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                if (mUser != null) {
                    mUser.getIdToken(true)
                            .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                public void onComplete(@NonNull Task<GetTokenResult> task) {
                                    if (task.isSuccessful()) {
                                        String idToken = task.getResult().getToken();
                                        // Send token to your backend via HTTPS
                                        // ...
                                        Log.d("TOKEN", idToken);
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

//        ImageView imageView;
//        TextView textView;

        public FilesViewHolder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.files_cardview);
            dayDateTextView = itemView.findViewById(R.id.files_cardview_day_date_tv);
            timeTextView = itemView.findViewById(R.id.files_cardview_time_tv);
            itemNameTextView = itemView.findViewById(R.id.files_cardview_item_name_tv);

//            imageView = itemView.findViewById(R.id.imageView);
//            textView = itemView.findViewById(R.id.textView);
        }
    }
}