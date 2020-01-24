package com.example.pavan.galleryview


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




import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.dragselectrecyclerview.DragSelectTouchListener
import com.afollestad.dragselectrecyclerview.Mode
import com.afollestad.materialcab.attached.AttachedCab
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.afollestad.materialcab.createCab
import com.afollestad.recyclical.datasource.emptySelectableDataSource
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.viewholder.isSelected
import com.afollestad.recyclical.withItem
import com.example.pavan.galleryview.Utils.Extensions.color
import com.example.pavan.galleryview.Utils.Extensions.setLightNavBar
import com.example.pavan.galleryview.Utils.Extensions.setToast
import com.example.pavan.galleryview.Utils.asDragSelectReceiver
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.ImageSize
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


@Suppress("unchecked")
class MainActivity : AppCompatActivity() {


    var filesNames:ArrayList<String> = ArrayList()
    var bitmapArray:ArrayList<Bitmap> = ArrayList()
    val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath+"/Camera"

    internal lateinit var touchListener: DragSelectTouchListener
    private val dataSource = emptySelectableDataSource().apply {
        onSelectionChange { invalidateCab() }
    }

    private var activeCab: AttachedCab? = null

    private val list by lazy { findViewById<RecyclerView>(R.id.listItems) }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1001)
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                //do something when permissons are granted
                getFromSdcard()
                setDataToApp()

            }else{
                requestPermissions()
            }
    }





    override fun onCreate(savedInstanceState: Bundle?) {

        setTheme(R.style.SplashTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this))


        touchListener = DragSelectTouchListener.create(this,
                dataSource.asDragSelectReceiver()){
            this.mode=Mode.PATH }

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions()
        }else{
            getFromSdcard()
            setDataToApp()
        }
        setLightNavBar()
    }



    private fun setDataToApp(){


        dataSource.set(filesNames
                .dropLastWhile { it.isEmpty() }
                .map(::MyItem)
        )

        list.setup {
            withLayoutManager(GridLayoutManager(this@MainActivity,2))
            withDataSource(dataSource)
            withItem<MyItem,MyItemViewHolder>(R.layout.griditems){
                onBind(::MyItemViewHolder){index: Int, item: MyItem ->
                    itemText!!.text=item.letter
                    setImagetoImageView(imageView,item.letter,index)
//                    itemSquare.setBackgroundColor(Color.parseColor("#03A9F4"))
                    var context: Context = itemView.context
                    var foreground:Drawable ?= null
                    if(isSelected()){
                        foreground = ColorDrawable(context.color(R.color.grid_foreground_selected))
                        itemText.setTextColor(context.color(R.color.grid_label_text_selected))
                    }else{
                        itemText.setTextColor(context.color(R.color.grid_label_text_normal))
                    }
                    itemSquare.foreground=foreground
                }
                onClick {
                    if(isSelected() || hasSelection()){
                        toggleSelection()
                    } else {
                        val intent = Intent(this@MainActivity, ShowImagesActivity::class.java)
                        intent.putStringArrayListExtra("fileNames", filesNames)
                        intent.putExtra("clickedItemIndex",Integer.valueOf(it.toString()))
                        startActivityForResult(intent,2902)
                    }
                }
                onLongClick { touchListener.setIsActive(true,it) }
            }
        }
        list.addOnItemTouchListener(touchListener)

        runOnUiThread{
            Thread.sleep(1000)
            constraintLayout.setBackgroundColor(this.color(R.color.colorPrimary))
        }



    }




    private fun setImagetoImageView(imageView: ImageView,imagePath:String,index:Int){

        val imageLoader = ImageLoader.getInstance()
        val imgFile = File("$path/$imagePath")
        imageLoader.displayImage(Uri.fromFile(imgFile).toString(),imageView, ImageSize(100,100))

    }





    private fun getFromSdcard() {
        val directory = File(path)
        val files = directory.listFiles { dir, name -> name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") }
        Log.i("FILE PATHS","$path ${files!!.size}")

        Arrays.sort(files) { o1, o2 ->
            o1.lastModified().compareTo(o2.lastModified())
        }
        files.reverse()
        filesNames.clear()
        if(files.isNotEmpty()){
        for (file in files){
            filesNames.add(file.name)
         }
        }else{
            filesNames.add("No Images")
        }

    }


    private fun requestPermissions(){
        val permissions=ArrayList<String>()
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if(permissions.size != 0)
            ActivityCompat.requestPermissions(this,permissions.toTypedArray(),1001)
    }


    private fun invalidateCab() {
        if(dataSource.hasSelection()){
            val count = dataSource.getSelectionCount()
            if(activeCab.isActive()){
                activeCab?.title(literal = getString(R.string.select_items,count))

            }else{
                activeCab=createCab(R.id.select_dialog_toolbar){
                    menu(R.menu.when_selected)
                    closeDrawable(android.R.drawable.ic_menu_close_clear_cancel)
                    titleColor(literal = Color.GREEN)
                    title(literal = getString(R.string.select_items,count))


                    onSelection{
                        if(it.itemId == R.id.done) {
                            val selectionString = (0 until dataSource.size())
                                    .filter { index -> dataSource.isSelectedAt(index) }
                                    .joinToString()
                            dataSource.deselectAll()
//                            Log.e("SELECTED", "$selectionString count")
                            setToast("Selected Letters:$selectionString")
                            dataSource.deselectAll()
                            true
                        }else
                            false
                        }

                    onDestroy {
                        dataSource.deselectAll()
                        true
                    }

                    }

                }
            }else{
            activeCab.destroy()
        }

        }


    override fun onBackPressed() {
        if(!activeCab.destroy())
            super.onBackPressed()
    }


    override fun onDestroy() {
        ImageLoader.getInstance().clearMemoryCache()
        ImageLoader.getInstance().clearDiskCache()
        super.onDestroy()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode==2902){

            if(ShowImagesActivity.fileNames.size!=filesNames.size){

                getFromSdcard()
                dataSource.set(filesNames
                        .dropLastWhile { it.isEmpty() }
                        .map(::MyItem)
                )
                list.adapter!!.notifyDataSetChanged()
            }
            list.layoutManager!!.scrollToPosition(ShowImagesActivity.currentPosition)


        }
        super.onActivityResult(requestCode, resultCode, data)
    }




}