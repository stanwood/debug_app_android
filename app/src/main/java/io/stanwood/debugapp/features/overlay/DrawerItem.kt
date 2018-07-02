package io.stanwood.debugapp.features.overlay

import android.view.View

data class DrawerItem (val title:String,val iconResId:Int,val id:Int,val itemClickListener : (DrawerItem) -> Unit ){
    fun onClicked(view: View?){
        itemClickListener(this)
    }
}

