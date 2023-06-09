package com.antonin.friendswave

import android.app.Application

import com.antonin.friendswave.data.firebase.FirebaseSourceEvent
import com.antonin.friendswave.data.firebase.FirebaseSourceUser
import com.antonin.friendswave.data.repository.EventRepo
import com.antonin.friendswave.data.repository.UserRepo
import com.antonin.friendswave.ui.viewModel.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Classe permettant de gerer l'application et les dépendances pour les View Model
// https://medium.com/@RedthLight/kodein-viewmodels-4023b7bf4920

class FriendWaveApp : Application(), KodeinAware {

    override val kodein = Kodein.lazy {
        import(androidXModule(this@FriendWaveApp))
        bind() from singleton {FirebaseSourceUser()}
        bind() from singleton {FirebaseSourceEvent()}
        bind() from singleton { UserRepo(instance()) }
        bind() from singleton { EventRepo(instance()) }
        bind() from provider { ContactViewModelFactory(instance()) }
        bind() from provider { AuthViewModelFactory(instance()) }
        bind() from provider { HomeFragmentVMFactory(instance()) }
        bind() from provider { EventFragmentVMFactory(instance(), instance()) }
        bind() from provider { NotifFragmentVMFactory(instance(), instance()) }
        bind() from provider { ChatVMFactory(instance(), instance()) }


    }
}
