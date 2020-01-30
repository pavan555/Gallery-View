/* *********************************************************************

 * Copyright [Sai Pavan Kumar](https://github.com/pavan555) 2020
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 *********************************************************************
 */


package com.example.pavan.galleryview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.pavan.galleryview.Utils.Extensions;
import com.google.android.material.snackbar.Snackbar;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.ortiz.touchview.TouchImageView;

import java.io.File;
import java.util.ArrayList;


public class ShowImagesActivity extends AppCompatActivity {


    static ArrayList<String> fileNames=new ArrayList<>();
    public static int currentPosition;
    public ViewPager viewPager;
    ImagePagerAdapter imagePagerAdapter;
    final static String PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/Camera/";


    Button shareButton;
    Button deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_images);

        shareButton=findViewById(R.id.share);
        deleteButton=findViewById(R.id.delete);

        fileNames=getIntent().getStringArrayListExtra("fileNames");
        int clickedIndex = getIntent().getIntExtra("clickedItemIndex", 0);

        currentPosition=clickedIndex;

        viewPager=findViewById(R.id.viewPager);
        imagePagerAdapter=new ImagePagerAdapter(this,fileNames);
        viewPager.setAdapter(imagePagerAdapter);
        viewPager.setCurrentItem(clickedIndex);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //when page is scrolling this method will be called
            }

            @Override
            public void onPageSelected(int position) {
                //when page is selected this method will be called once
                currentPosition=position;

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewPager.setOffscreenPageLimit(1);
    }


    public static   class ImagePagerAdapter extends PagerAdapter {
        private ArrayList<String> imageFileNames;


        LayoutInflater layoutInflater;
        DisplayImageOptions options;
        Context context;


        public ImagePagerAdapter(Context context,ArrayList<String> imageFileNames) {
            this.context=context;
            this.imageFileNames=imageFileNames;
            layoutInflater=LayoutInflater.from(context);
            options=new DisplayImageOptions.Builder()
                    .showImageForEmptyUri(R.drawable.ic_empty)
                    .showImageOnFail(R.drawable.ic_error)
                    .resetViewBeforeLoading(true)
                    .cacheOnDisk(true)
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .considerExifParams(true)
                    .displayer(new FadeInBitmapDisplayer(300))
                    .build();
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup view, int position) {

            final View imageLayout=layoutInflater.inflate(R.layout.item_image_pager,view,false);
            assert imageLayout!=null;


            TouchImageView imageView=imageLayout.findViewById(R.id.itemImagePager);
            final ProgressBar progressBar=imageLayout.findViewById(R.id.loading);

            ImageLoader.getInstance().displayImage("file://"+PATH+fileNames.get(position), imageView, options, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        String message=null;
                        switch (failReason.getType()){
                            case UNKNOWN:
                                message="Unknown error";
                                break;
                            case IO_ERROR:
                                message="I/O error";
                                break;
                            case NETWORK_DENIED:
                                message="Network Denied";
                                break;
                            case OUT_OF_MEMORY:
                                message="Out of memory";
                                break;
                            case DECODING_ERROR:
                                message="decoding error";
                                break;
                        }
                        Extensions.INSTANCE.setToast(context,message);
                        progressBar.setVisibility(View.GONE);

                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });

            view.addView(imageLayout,0);

            return imageLayout;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View)object);
        }

        @Override
        public int getCount() {
            return imageFileNames.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            ((TouchImageView)view.findViewById(R.id.itemImagePager)).resetZoom();
            return view.equals(object);
        }

        @Override
        public void restoreState(@Nullable Parcelable state, @Nullable ClassLoader loader) {
            super.restoreState(state, loader);
        }




        @Nullable
        @Override
        public Parcelable saveState() {
            return super.saveState();
        }
    }


    public void delete(final View view) {

        final File file = new File(PATH + fileNames.get(currentPosition));
        final int removedPos = currentPosition;

        deleteButton.setClickable(false);
        if (file.exists()) {
            fileNames.remove(currentPosition);
            updateView(removedPos);

            Snackbar snackbar = Snackbar.make(view.getRootView(), "Deleted", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO !?", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fileNames.add(removedPos, file.getName());
                    updateView(removedPos);
                    deleteButton.setClickable(true);
                }
            });
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {

                    if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                        if (!file.delete()) {
                            Extensions.INSTANCE.setToast(view.getContext(), "not deleted");

                        }
                        deleteButton.setClickable(true);

                    }
                    super.onDismissed(transientBottomBar, event);
                }

                @Override
                public void onShown(Snackbar sb) {
                    super.onShown(sb);
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();


        } else {
            Extensions.INSTANCE.setToast(view.getContext(), "file not exists");
        }

    }

    public void share(View view) {

        shareButton.setClickable(false);
        deleteButton.setClickable(false);
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        final File photoFile = new File(PATH+fileNames.get(currentPosition));
        shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", photoFile));
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        /*

        ******* TEMPORARY SOLUTION WHICH WORKS ONLY BELOW ANDROID VERSION 10 ************************
                if(Build.VERSION.SDK_INT>=24){
                    try {
                        Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                        m.invoke(null);
                    }catch (Exception e){
                            e.printStackTrace();
                    }
                }
        ***********************************************************************************************/

        startActivityForResult(Intent.createChooser(shareIntent, "Share image using"),2244);
    }


    public void updateView(int pos){
        viewPager.setAdapter(null);
        imagePagerAdapter=new ImagePagerAdapter(ShowImagesActivity.this,fileNames);
        viewPager.setAdapter(imagePagerAdapter);
        viewPager.setCurrentItem(pos, true);
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==2244){

            shareButton.setClickable(true);
            deleteButton.setClickable(true);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        ImageLoader.getInstance().clearMemoryCache();
        ImageLoader.getInstance().clearDiskCache();
        super.onDestroy();
    }
}
