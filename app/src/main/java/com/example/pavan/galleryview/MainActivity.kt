package com.example.pavan.galleryview


import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
import com.example.pavan.galleryview.Utils.BitmapHelper.decodeBitmapFromFile
import com.example.pavan.galleryview.Utils.Extensions.color
import com.example.pavan.galleryview.Utils.Extensions.setLightNavBar
import com.example.pavan.galleryview.Utils.Extensions.setToast
import com.example.pavan.galleryview.Utils.asDragSelectReceiver
import java.io.File


@Suppress("unchecked")
class MainActivity : AppCompatActivity() {


//    val ALPHABET = "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z"
//            .split(" ")

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
            if(grantResults.size>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                //do something when permissons are granted
            }else{
                requestPermissions()
            }
    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))


        requestPermissions()
        getFromSdcard()

        touchListener = DragSelectTouchListener.create(this,
                dataSource.asDragSelectReceiver()){
            this.mode=Mode.PATH }

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
                onClick { toggleSelection() }
                onLongClick { touchListener.setIsActive(true,it) }
            }
        }
        list.addOnItemTouchListener(touchListener)

        setLightNavBar()
    }



    private fun setImagetoImageView(imageView: ImageView,imagePath:String,index:Int){

        if(index >= bitmapArray.size){
            Log.i("BITMAPS","ENTERED INTO IMAGEFILE")
            val imgFile = File("$path/$imagePath")
            if (imgFile.exists()) {
                 val myBitmap = decodeBitmapFromFile(imgFile.absolutePath,50,50)
                 bitmapArray.add(index, myBitmap!!)
                 imageView.setImageBitmap(myBitmap)
            }

        }else{
            imageView.setImageBitmap(bitmapArray.get(index))
        }
    }





    private fun getFromSdcard() {
        val directory = File(path)
        val files = directory.listFiles { dir, name -> name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") }
        Log.i("FILE PATHS","$path ${files!!.size}")

        if(files.isNotEmpty()){
        for (file in files){
            filesNames.add(file.name)
            val myBitmap = decodeBitmapFromFile("$path/${file.name}",100,100)
            bitmapArray.add(myBitmap!!)

         }
        }else{
            filesNames.add("No Images")
            bitmapArray.add(BitmapFactory.decodeResource(resources,R.drawable.ic_not_interested_black_24dp))
        }

    }


    private fun requestPermissions(){
        val permissions=ArrayList<String>()
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
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



}