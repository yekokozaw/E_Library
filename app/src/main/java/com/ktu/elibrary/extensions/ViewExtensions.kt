package com.ktu.elibrary.extensions

import android.app.AlertDialog
import android.content.DialogInterface
import android.view.View

fun View.hide(){
    visibility = View.GONE
}

fun View.show(){
    visibility = View.VISIBLE
}

fun View.invisible(){
    visibility = View.INVISIBLE
}