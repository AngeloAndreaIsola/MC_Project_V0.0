package com.example.mc_project_v00;

import android.util.Log;

import com.example.mc_project_v00.database.Post;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PostModel {
    private List<JSONObject> postList;
    private List<Post> postListForDB;
    private static PostModel theInstance = null;
    private static final String TAG = "POST Model";


    private PostModel(){
        postList = new ArrayList<JSONObject>();
        postListForDB = new ArrayList<Post>();
    }

    public static synchronized PostModel getInstance(){
        if(theInstance==null){
            theInstance= new PostModel();
        }
        return theInstance;
    }

    public int getPostListSize(){
        return postList.size();
    }

    public JSONObject getPostFromList(int i) throws JSONException {
        JSONObject o = postList.get(i);
        return o;
    }

    public List<JSONObject> getPostList () {
        return postList;
    }

    public Post getPostFromListForDB(int i) throws JSONException {
        Post o = postListForDB.get(i);
        return o;
    }

    public List<Post> getListForDB() throws JSONException {
        return postListForDB;
    }

    public int getListForDBsize() {
        return postListForDB.size();
    }


    public void addPosts(JSONObject response) throws JSONException {
        postList.clear();
        JSONArray jsonArray = response.getJSONArray("posts");
        for (int i = 0; i<jsonArray.length(); i++) {
            postList.add( jsonArray.getJSONObject(i));
        }
        Log.d(TAG, "Post salvati nel postModel: " + postList.toString());


    }

    public void addPostForDB(JSONObject response) throws JSONException{
        JSONArray jsonArray = response.getJSONArray("posts");
        for (int i = 0; i<jsonArray.length(); i++) {
            Post post = new Post();
            post.setUid( jsonArray.getJSONObject(i).getString("uid"));
            post.setUsername( jsonArray.getJSONObject(i).getString("name"));
            post.setPid( jsonArray.getJSONObject(i).getString("pid"));
            post.setType( jsonArray.getJSONObject(i).getString("type"));
            post.setVersion(jsonArray.getJSONObject(i).getInt("pversion"));

            if (!jsonArray.getJSONObject(i).isNull("content"))
                post.setContentTextPost(jsonArray.getJSONObject(i).getString("content"));

            if (!jsonArray.getJSONObject(i).isNull("lat"))
                post.setLat( jsonArray.getJSONObject(i).getString("lat"));

            if (!jsonArray.getJSONObject(i).isNull("lon"))
                post.setLonN( jsonArray.getJSONObject(i).getString("lon"));


            postListForDB.add(post);
        }
        Log.d(TAG, "Post salvati nela lista per database: " + postListForDB.toString());

    }

    public void testAddWrongType() throws JSONException {
        postList.get(0).put("type", "K");
    }

}
