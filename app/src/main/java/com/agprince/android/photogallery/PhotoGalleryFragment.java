package com.agprince.android.photogallery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.PluralsRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends VisibleFragment {

    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        updateItems();
        //Intent i = PollService.newIntent(getActivity());
        //getActivity().startService(i);
        PollService.setServiceAlarm(getActivity(),true);
        Handler repsonseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(repsonseHandler);
        mThumbnailDownloader.setOnThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail) {
                Drawable drawable = new BitmapDrawable(getResources(),thumbnail);
                target.bindDrawable(drawable);
            }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        LogUtil.d("Background thread start");

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_photo_gallery,container,false);
        mPhotoRecyclerView = view.findViewById(R.id.photo_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));

        setAdapter();

        return view;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery,menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                LogUtil.d("QueryTextSubmit: "+query);
                QueryPreferences.setStoredQuery(getActivity(),query);
                updateItems();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                LogUtil.d("QueryTextChange: "+newText);
                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query,false);
            }
        });
        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if(PollService.isServiceAlarmOn(getActivity())){
            toggleItem.setTitle(R.string.stop_polling);
        }else {
            toggleItem.setTitle(R.string.start_polling);
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(),null);
                updateItems();
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(),shouldStartAlarm);
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateItems(){
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemTask(query).execute();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        LogUtil.d(" Background thread destroyed");
    }

    private void setAdapter(){
        if(isAdded()){
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }
    private class FetchItemTask extends AsyncTask<Void,Void,List<GalleryItem>>{
        private String mQuery;

        public FetchItemTask(String query){
            mQuery = query;
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... voids) {

            if (mQuery == null) {
                return new FlickrFetchr().fetchRecentPhotos();
            } else {
                return new FlickrFetchr().searchPhotos(mQuery); }
        }



        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            super.onPostExecute(galleryItems);
            mItems = galleryItems;
            setAdapter();

        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder{

        private ImageView mImageView;

        public PhotoHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.item_image_view);
        }
        public void bindDrawable(Drawable drawable){
            mImageView.setImageDrawable(drawable);

        }
    }
    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{

        private List<GalleryItem> mItems;

        public PhotoAdapter(List<GalleryItem> galleryItems){
            mItems = galleryItems;
        }

        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View v  = inflater.inflate(R.layout.list_item_gallery,parent,false);


            return new PhotoHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {

            GalleryItem item = mItems.get(position);

            Drawable placeHolder = getResources().getDrawable(R.drawable.bill_up_close);

            holder.bindDrawable(placeHolder);
            mThumbnailDownloader.queueThumbnail(holder,item.getUrl());
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }
    }


}
