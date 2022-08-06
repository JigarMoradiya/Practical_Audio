package com.jigar.audioplayer.utils.extensions

import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.app.ActivityCompat


fun Context.toastS(message:String){
    Toast.makeText( this , message , Toast.LENGTH_SHORT).show()
}
val Context.layoutInflater: LayoutInflater
    get() = getSystemServiceAs(Context.LAYOUT_INFLATER_SERVICE)

fun <T> Context.getSystemServiceAs(serviceName: String) = getSystemService(serviceName) as T

fun Context.isPermissionGrant(permission : String) : Boolean{
    return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}