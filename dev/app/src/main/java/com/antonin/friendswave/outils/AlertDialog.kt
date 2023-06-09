package com.antonin.friendswave.outils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: C'est une classe permettant d'afficher des alert Dialog

// classe inspiré par https://www.javatpoint.com/android-alert-dialog-example et la doc android

class AlertDialog: DialogInterface {

    fun showDialog(context: Context, title: String, msg: String,
                   positiveBtnText: String, negativeBtnText: String?,
                   positiveBtnClickListener: DialogInterface.OnClickListener,
                   negativeBtnClickListener: DialogInterface.OnClickListener?): AlertDialog {
        val builder = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(msg)
            .setCancelable(true)
            .setPositiveButton(positiveBtnText, positiveBtnClickListener)
        if (negativeBtnText != null)
            builder.setNegativeButton(negativeBtnText, negativeBtnClickListener)
        val alert = builder.create()
        alert.show()
        return alert
    }

    override fun cancel() {
        return
    }

    override fun dismiss() {
        return
    }

}