package com.agprince.android.photogallery;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlickrFetchr {
    private static final String API_KEY = "24731396ff757f8bf0d9529733a3ea59";


    public List<GalleryItem> fetchItems(int page) {

        List<GalleryItem> items = new ArrayList<>();

        try {
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("page",Integer.toString(page))
                    .appendQueryParameter("extras", "url_s")
                    .build().toString();
            String jsonString = getUrlString(url);
            Gson gson = new Gson();
            PhotoRequestResult result = gson.fromJson(jsonString,PhotoRequestResult.class);

            //JSONObject jsonBody = new JSONObject(jsonString);
            //parseItem(items,jsonBody);
            items = result.getResults();
            LogUtil.i("Received JSON: " + jsonString);
        } catch (IOException ioe) {
            LogUtil.e("Failed to fetch items", ioe);
        } catch (Exception e) {
            LogUtil.e("Failed to parse json ", e);
        }
        return items;
    }

    private void parseItem(List<GalleryItem> items, JSONObject jsonBody) throws JSONException {

        JSONObject photosJson = jsonBody.getJSONObject("photos");
        JSONArray photosJsonArray = photosJson.getJSONArray("photo");
        Type galleyItemType = new TypeToken<ArrayList<GalleryItem>>(){}.getType();
        Gson gson = new Gson();
        String jsonPhotosString = photosJsonArray.toString();

        List<GalleryItem> galleryItemList =  gson.fromJson(jsonPhotosString,galleyItemType);
        items.addAll(galleryItemList);

//
//        for (int i = 0; i < photosJsonArray.length(); i++) {
//            JSONObject photoJson = photosJsonArray.getJSONObject(i);
//            GalleryItem item = new GalleryItem();
//            item.setId(photoJson.getString("id"));
//            item.setCaption(photoJson.getString("title"));
//            if(!photoJson.has("url_s")){
//                continue;
//            }
//            item.setUrl(photoJson.getString("url_s"));
//            items.add(item);
//
//        }

    }

    public byte[] getUrlBytes(String urlSpec) throws IOException {

        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            InputStream inputStream = connection.getInputStream();

            int bytesRead = 0;
            byte[] buf = new byte[1024];
            while ((bytesRead = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, bytesRead);
            }
            outputStream.close();
            return outputStream.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {

        return new String(getUrlBytes(urlSpec));
    }

}
