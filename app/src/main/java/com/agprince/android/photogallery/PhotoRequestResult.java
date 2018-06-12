package com.agprince.android.photogallery;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PhotoRequestResult {

    PhotoResults photos;

    class PhotoResults {
        int page;
        int pages;
        int perpage;
        int total;
        @SerializedName("photo")
        List<GalleryItem> photolist;

        List<GalleryItem> getPhotolist() {
            return photolist;
        }

        int getItemsPerpage() {
            return perpage;
        }

        int getMaxPages() {
            return pages;
        }

        int getTotal() {
            return total;
        }
    }

    String stat;

    List<GalleryItem> getResults() {
        return photos.photolist;
    }

    int getPageCount() {
        return photos.getMaxPages();
    }

    int getItemCount() {
        return photos.getTotal();
    }

    int getItemsPerpage() {
        return photos.getItemsPerpage();
    }

}
