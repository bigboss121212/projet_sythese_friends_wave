package com.antonin.friendswave.data.firebase

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.antonin.friendswave.R

//Documentation https://firebase.google.com/docs/storage/android/create-reference?hl=fr

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: C'est le lien entre la base de donnée Firebase et notre application, elle s'occupe de recupérer les photos et les afficher.

class FirebaseStore {


    var storage: FirebaseStorage = Firebase.storage

    fun displayImage(imgView:ImageView, path: String){

        val storageRef = storage.reference.child(path)

        storageRef.downloadUrl.addOnSuccessListener {

            Glide.with(imgView.context)
                    .load(it)
                .placeholder(R.drawable.wave1)
                    .apply(RequestOptions().override(100, 100))
                    .centerCrop()
                    .into(imgView)
            }.addOnFailureListener {
                println(it)
            }

    }





}