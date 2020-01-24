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
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.pavan.galleryview.Utils.Extensions;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.util.ArrayList;


public class ShowImagesActivity extends AppCompatActivity {


    static ArrayList<String> fileNames=new ArrayList<>();
    public static int currentPosition;
    public ViewPager viewPager;
    ImagePagerAdapter imagePagerAdapter;
    final static String PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/Camera/";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_images);


        fileNames=getIntent().getStringArrayListExtra("fileNames");
        int clickedIndex = getIntent().getIntExtra("clickedItemIndex", 0);

        currentPosition=clickedIndex;

        viewPager=findViewById(R.id.viewPager);
        imagePagerAdapter=new ImagePagerAdapter(this);
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

    }


    public static   class ImagePagerAdapter extends PagerAdapter {
        private static final ArrayList<String> imageFileNames=ShowImagesActivity.fileNames;


        LayoutInflater layoutInflater;
        DisplayImageOptions options;
        Context context;



        public ImagePagerAdapter(Context context) {
            this.context=context;
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


            ImageView imageView=imageLayout.findViewById(R.id.itemImagePager);
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
            return view.equals(object);
        }

        @Override
        public void restoreState(@Nullable Parcelable state, @Nullable ClassLoader loader) {
            super.restoreState(state, loader);
        }

        public void removeView(int index) {
            imageFileNames.remove(index);
            notifyDataSetChanged();
        }

        @Nullable
        @Override
        public Parcelable saveState() {
            return super.saveState();
        }
    }


    public void delete(final View view) {

        new AlertDialog.Builder(view.getContext())
                .setTitle("Are You Sure?")
                .setIcon(R.drawable.ic_delete)
                .setCancelable(false)
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final File file=new File(PATH+fileNames.get(currentPosition));
                        fileNames.remove(currentPosition);
                        imagePagerAdapter.notifyDataSetChanged();
                        if(file.exists()){
                            if(file.delete()) {
                                Extensions.INSTANCE.setToast(view.getContext(), "deleted successfully");
                                imagePagerAdapter.removeView(currentPosition);
                                /*this is where update occurs*/
                                viewPager.setAdapter(imagePagerAdapter);
                                viewPager.setCurrentItem(currentPosition + 1, true);
                            }else{
                                Extensions.INSTANCE.setToast(view.getContext(),"not deleted");
                            }
                        }else{
                            Extensions.INSTANCE.setToast(view.getContext(),"file not exists");
                        }
                    }
                })
                .setNegativeButton("no",null)
                .show();


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
