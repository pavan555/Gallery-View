package com.example.pavan.galleryview.Utils

import com.afollestad.dragselectrecyclerview.DragSelectReceiver
import com.afollestad.recyclical.datasource.SelectableDataSource

fun SelectableDataSource<*>.asDragSelectReceiver(): DragSelectReceiver {
    return object : DragSelectReceiver {
        override fun getItemCount(): Int = size()

        override fun setSelected(
                index: Int,
                selected: Boolean
        ) {
            if (selected) selectAt(index) else deselectAt(index)
        }

        override fun isSelected(index: Int): Boolean = isSelectedAt(index)

        override fun isIndexSelectable(index: Int): Boolean = true
    }
}
