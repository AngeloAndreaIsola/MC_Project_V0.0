package com.example.mc_project_v00;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.example.mc_project_v00.database.AppExecutors;
import com.example.mc_project_v00.database.DatabaseClient;
import com.example.mc_project_v00.database.PostContentImage;
import com.example.mc_project_v00.database.PostProfileImage;
import com.example.mc_project_v00.database.PostRoomDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostViewHolder> {
    private static final String TAG ="Post RecyclerView";
    private List<JSONObject> postList;
    private Context contextContainer;
    private ComunicationController ccPostAdapter;
    private String sid;
    private View.OnClickListener mClickListener = null;
    private PostRoomDatabase postRoomDatabase;



    public int getItemViewType(int position){
        try {
            if(postList.get(position).getString("type").contains("t")){
                return 0;
            }else if (postList.get(position).getString("type").contains("i")) {
                return 1;
            }else{
                return 2;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return 3;
        }

    }

    public PostAdapter (Context context, String sid, View.OnClickListener ClickListener) throws JSONException {
        /*
        List<JSONObject> tempPostList = new ArrayList<JSONObject>();;
        JSONArray postArray = object.getJSONArray("posts");
        for(int i=0; i < postArray.length(); i++ ){
            tempPostList.add((JSONObject) postArray.get(i));
        }

         */

        this.postList = PostModel.getInstance().getPostList();

        this.contextContainer = context;

        this.ccPostAdapter = new ComunicationController(context);

        this.sid = sid;

        mClickListener = ClickListener;

        postRoomDatabase = DatabaseClient.getInstance(contextContainer).getPostRoomDatabase();

    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(contextContainer);
        View view;
        if (viewType == 0) {
            view = layoutInflater.inflate(R.layout.post_text, parent, false);
            return new PostViewHolder(view);
        }else if (viewType == 1){
            view = layoutInflater.inflate(R.layout.post_image, parent, false);
            return new PostViewHolder(view);
        } else {
            view = layoutInflater.inflate(R.layout.post_position, parent, false);
            return new PostViewHolder(view);
        }

    }


    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        try {
            if(postList.get(position).getString("type").contains("t")){ //BIND VIEWHOLDERONE

                PostViewHolder.ViewHolder_Post_Text viewHolderPostText = new PostViewHolder.ViewHolder_Post_Text(holder.itemView, mClickListener);

                //TEXT SET USERNAME
                setUsername(viewHolderPostText, R.id.post_Text_Username, position, postList.get(position).getString("uid"));


                //TEXT FOTO PROFILO: CONTROLLA SE E' NEL DB ALTRIMENTI FA LA CHIAMATA
                int uid = postList.get(position).getInt("uid");
                if (postList.get(position).getInt("pversion")==0 || postRoomDatabase.postDao().getProfileVersion(uid) >= postList.get(position).getInt("pversion")) {
                    loadProfilePicture(viewHolderPostText, position, R.id.post_Text_ProfileImage);
                }else {
                    profileImageRequest(position, viewHolderPostText, R.id.post_Text_ProfileImage);
                }


                //TEXT SET CONTENT
                TextView content = viewHolderPostText.itemView.findViewById(R.id.post_text_Content);
                JSONObject object_content = postList.get(position);
                String temp_content = object_content.getString("content");
                content.setText(temp_content);

            }else if(postList.get(position).getString("type").contains("i")) { //BIND VIEWHOLDERONE

                PostViewHolder.ViewHolder_Post_Image viewHolderPostImage = new PostViewHolder.ViewHolder_Post_Image(holder.itemView, mClickListener, position);

                //IMAGE SET USERNAME
                setUsername(viewHolderPostImage, R.id.post_Image_Username, position, postList.get(position).getString("uid"));

                //IMAGE FOTO PROFILO: CONTROLLA SE E' NEL DB ALTRIMENTI FA LA CHIAMATA
                int uid = postList.get(position).getInt("uid");
                if (postList.get(position).getInt("pversion")==0 || postRoomDatabase.postDao().getProfileVersion(uid) >= postList.get(position).getInt("pversion")) {
                    loadProfilePicture(viewHolderPostImage, position, R.id.post_Image_ProfileImage);
                }else {
                    profileImageRequest(position, viewHolderPostImage, R.id.post_Image_ProfileImage);
                }

                //IMAGE FAI LA CHIAMATA PER PRENDERE L'IMMAGINE CONTENUTO
                String pid = postList.get(position).getString("pid");

                if (postRoomDatabase.postDao().getContentImage(Integer.parseInt(pid)) != null){

                    String encodedImage = (postRoomDatabase.postDao().getContentImage(Integer.parseInt(pid)));

                    ImageView content = viewHolderPostImage.itemView.findViewById(R.id.post_Image_Content);
                    try {
                        //decodifica da stringa a bitmap
                        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                content.setImageBitmap(decodedByte);
                                Log.d(TAG, "contenuto immagine caricato da db");
                    } catch (IllegalArgumentException e) {
                        Log.d(TAG, "BASE 64 SBAGLIATO");
                        content.setImageResource(R.drawable.ic_baseline_broken_image_24);
                    }

                }else{
                    ccPostAdapter.getPostImage(sid, pid, response -> {
                        try {
                            handleGetPostImageResponse(response, viewHolderPostImage, position);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, error -> reportErrorToUser(error));
                }




            }else if(postList.get(position).getString("type").contains("l")){

                PostViewHolder.ViewHolder_Post_Position viewHolderPostPosition = new PostViewHolder.ViewHolder_Post_Position(holder.itemView, mClickListener, position);

                //POSITION: SET USERNAME
                setUsername(viewHolderPostPosition, R.id.post_Position_Username, position, postList.get(position).getString("uid"));

                //POSITION FOTO PROFILO: CONTROLLA SE E' NEL DB ALTRIMENTI FA LA CHIAMATA
                int uid = postList.get(position).getInt("uid");
                if (postList.get(position).getInt("pversion")==0 || postRoomDatabase.postDao().getProfileVersion(uid) >= postList.get(position).getInt("pversion")) {
                    loadProfilePicture(viewHolderPostPosition, position, R.id.post_Position_ProfileImage);
                }else {
                    profileImageRequest(position, viewHolderPostPosition, R.id.post_Position_ProfileImage);
                }


                //POSITION SET CONTENT
                Button showPositionButton = viewHolderPostPosition.itemView.findViewById(R.id.post_Position_Button_ShowPosition);;
                if ( ! (postList.get(position).getDouble("lat") <= 90.0 && postList.get(position).getDouble("lat")>= -90.0 && postList.get(position).getDouble("lon")<=180 && postList.get(position).getDouble("lon")>=-180)){
                    showPositionButton.setBackgroundColor(Color.GRAY);
                    showPositionButton.setText("POSIZIONE CONDIVISA NON VALIDA");
                    showPositionButton.setOnClickListener(null);

                }
            }

            if (UserData.f.toString().contains(postList.get(position).getString("uid"))){
               if(postList.get(position).getString("type").contains("l")){
                   holder.itemView.setBackgroundColor(Color.parseColor("#FF0000"));
                   //holder.itemView.findViewById(R.id.tv_date_p).setVisibility(View.INVISIBLE);
                   holder.itemView.findViewById(R.id.tv_date_p).setVisibility(View.VISIBLE);

                   Button follow = (Button)holder.itemView.findViewById(R.id.tv_date_p);
                   follow.setText("Non seguire: " + postList.get(position).getString("uid"));


               }
               if(postList.get(position).getString("type").contains("t")){
                   holder.itemView.setBackgroundColor(Color.parseColor("#FF0000"));
                   //holder.itemView.findViewById(R.id.tv_date_t).setVisibility(View.INVISIBLE);
                   holder.itemView.findViewById(R.id.tv_date_t).setVisibility(View.VISIBLE);


                   Button follow = (Button)holder.itemView.findViewById(R.id.tv_date_t);
                   follow.setText("Non seguire: " + postList.get(position).getString("uid"));
               }
               if(postList.get(position).getString("type").contains("i")){
                   holder.itemView.setBackgroundColor(Color.parseColor("#FF0000"));
                   //holder.itemView.findViewById(R.id.tv_date_i).setVisibility(View.INVISIBLE);
                   holder.itemView.findViewById(R.id.tv_date_i).setVisibility(View.VISIBLE);


                   Button follow = (Button)holder.itemView.findViewById(R.id.tv_date_i);
                   follow.setText("Non seguire" + postList.get(position).getString("uid"));
               }
            }else{
                holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));

                if(postList.get(position).getString("type").contains("l")){
                    Button follow = (Button)holder.itemView.findViewById(R.id.tv_date_p);
                    follow.setText("Segui");

                    holder.itemView.findViewById(R.id.tv_date_p).setVisibility(View.VISIBLE);
                    holder.itemView.findViewById(R.id.tv_date_p).setOnClickListener(new View.OnClickListener(){

                        @Override
                        public void onClick(View v) {
                            try {
                                Log.d(TAG, "Voglio seguire: " + postList.get(position).getString("uid"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                if(postList.get(position).getString("type").contains("t")){
                    Button follow = (Button)holder.itemView.findViewById(R.id.tv_date_t);
                    follow.setText("Segui");

                    holder.itemView.findViewById(R.id.tv_date_t).setVisibility(View.VISIBLE);
                    holder.itemView.findViewById(R.id.tv_date_t).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                Log.d(TAG, "Voglio seguire: " + postList.get(position).getString("uid"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                if(postList.get(position).getString("type").contains("i")){
                    Button follow = (Button)holder.itemView.findViewById(R.id.tv_date_i);
                    follow.setText("Segui");

                    holder.itemView.findViewById(R.id.tv_date_i).setVisibility(View.VISIBLE);
                    holder.itemView.findViewById(R.id.tv_date_i).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                Log.d(TAG, "Voglio seguire: " + postList.get(position).getString("uid"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void handleGetPostImageResponse(JSONObject response, PostViewHolder.ViewHolder_Post_Image viewHolderPostImage, int position ) throws JSONException {
        Log.d(TAG, "request post image correct: "+ response.toString());
        ImageView content = viewHolderPostImage.itemView.findViewById(R.id.post_Image_Content);
        try {
            //decodifica da stringa a bitmap
            String encodedImage = response.getString("content");
            byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            content.setImageBitmap(decodedByte);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "BASE 64 SBAGLIATO");
            content.setImageResource(R.drawable.ic_baseline_broken_image_24);
        }


        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                PostRoomDatabase postRoomDatabase = DatabaseClient.getInstance(contextContainer).getPostRoomDatabase();

                PostContentImage postContentImage = new PostContentImage();
                try {
                    postContentImage.setImage_pid(response.getString("pid"));
                    postContentImage.setPostContentImage(response.getString("content"));

                    postRoomDatabase.postDao().addContentImage(postContentImage);
                    Log.d(TAG, "immagine post salvata nel database");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }
    private void handleGetUSerPictureResponse(JSONObject response, PostViewHolder postViewHolder, int viewID) throws JSONException {
        Log.d(TAG, "request user picture correct: "+ response.toString());


        ImageView profilePicture = postViewHolder.itemView.findViewById(viewID);
        try {
            //decodifica da stringa a bitmap
            String encodedImage = response.getString("picture");
            byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            profilePicture.setImageBitmap(decodedByte);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "BASE 64 SBAGLIATO");
            profilePicture.setImageResource(R.drawable.ic_baseline_broken_image_24);
        }


        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                PostRoomDatabase postRoomDatabase = DatabaseClient.getInstance(contextContainer).getPostRoomDatabase();

                PostProfileImage postProfileImage = new PostProfileImage();
                try {
                    postProfileImage.setProfile_uid(response.getString("uid"));
                    postProfileImage.setProfileImage(response.getString("picture"));
                    postProfileImage.setVersion(response.getInt("pversion"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                postRoomDatabase.postDao().addPostProfileImage(postProfileImage);
                Log.d(TAG, "immagine proilo salvata nel database");
            }
        });





    }

    private void reportErrorToUser(VolleyError error) {
        Log.d(TAG, "request error: " + error.toString());
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void profileImageRequest(int position, PostViewHolder postViewHolder, int viewID) throws JSONException {
        String uid = postList.get(position).getString("uid");

        ccPostAdapter.getUserPicture(sid, uid, response -> {
            try {
                handleGetUSerPictureResponse(response,  postViewHolder, viewID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> reportErrorToUser(error));

    }

    public void setUsername(PostViewHolder postViewHolder, int viewID, int position, String uid) throws JSONException {
        TextView username = postViewHolder.itemView.findViewById(viewID);
        JSONObject object_username = postList.get(position);
        String temp_username = object_username.getString("name");
        Log.d(TAG, "il nome dell'utente è: " + temp_username + " UID: " + postList.get(position).getString("uid"));
        if (temp_username == "null"){
            username.setText("NO_NAME");
        }else{
            if (UserData.f.toString().contains(uid)){
                username.setText(temp_username + " (SEGUITO)");
                //username.setTextColor(Color.parseColor("#FF0000"));
            }else{
                username.setText(temp_username);
            }

        }
    }

    public void loadProfilePicture (PostViewHolder postViewHolder, int position, int viewID) throws JSONException {
        int uid = postList.get(position).getInt("uid");
        if (postList.get(position).getInt("pversion") == 0) {

            ImageView profilePicture = postViewHolder.itemView.findViewById(viewID);
            profilePicture.setImageResource(R.drawable.ic_baseline_account_box_24);

        } else if (postRoomDatabase.postDao().getProfileVersion(uid) >= postList.get(position).getInt("pversion")) {

            String encodedImage = (postRoomDatabase.postDao().getProfileContent(uid));
            ImageView content = postViewHolder.itemView.findViewById(viewID);

            try {
                //decodifica da stringa a bitmap
                byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                content.setImageBitmap(decodedByte);
                Log.d(TAG, "contenuto immagine profilo per post immagine caricato da db");
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "BASE 64 SBAGLIATO");

                content.setImageResource(R.drawable.ic_baseline_broken_image_24);
            }
        }
    }
}
