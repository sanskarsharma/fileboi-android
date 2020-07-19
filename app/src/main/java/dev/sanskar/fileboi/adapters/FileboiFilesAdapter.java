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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import dev.sanskar.fileboi.MainActivity;
import dev.sanskar.fileboi.R;
import dev.sanskar.fileboi.api.FileboiFile;
import dev.sanskar.fileboi.backend.FileboiAPI;
import dev.sanskar.fileboi.utilities.HttpUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class FileboiFilesAdapter extends RecyclerView.Adapter<FileboiFilesAdapter.FileboiFileViewHolder> {

    Context mCtx;
    List<FileboiFile> fileboiFileList;

    public FileboiFilesAdapter(Context mCtx, List<FileboiFile> fileboiFileList) {
        this.mCtx = mCtx;
        this.fileboiFileList = fileboiFileList;
    }

    @NonNull
    @Override
    public FileboiFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.recyclerview_layout, parent, false);
        return new FileboiFileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileboiFileViewHolder holder, int position) {
        final FileboiFile fileboiFile = fileboiFileList.get(position);

//        Glide.with(mCtx)
//                .load(hero.getImageurl())
//                .into(holder.imageView);

        holder.textView.setText(fileboiFile.getName());
        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), fileboiFile.getName(), Toast.LENGTH_SHORT).show();



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
                                                .url(FileboiAPI.getFileDownloadURL(fileboiFile.getId()))
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
        return fileboiFileList.size();
    }

    class FileboiFileViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textView;

        public FileboiFileViewHolder(View itemView) {
            super(itemView);

//            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.textView);
        }
    }
}