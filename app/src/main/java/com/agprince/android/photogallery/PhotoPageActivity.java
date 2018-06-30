package com.agprince.android.photogallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class PhotoPageActivity extends SingleFragmentActivity {

    public static Intent newIntance(Context context, Uri PhotoPageUri){
        Intent intent = new Intent(context,PhotoPageActivity.class);
        intent.setData(PhotoPageUri);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return PhotoPageFragment.newInstance(getIntent().getData());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.d("activity 111111");
    }
}
