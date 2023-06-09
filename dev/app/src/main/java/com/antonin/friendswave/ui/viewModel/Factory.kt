package com.antonin.friendswave.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.antonin.friendswave.data.repository.EventRepo
import com.antonin.friendswave.data.repository.UserRepo

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Classes représentant les factory pour nos View Models.

class AuthViewModelFactory( private val repository: UserRepo) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}

@Suppress("UNCHECKED_CAST")
class ChatVMFactory(private val repository: UserRepo, private val repoEvent: EventRepo) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repository, repoEvent) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ContactViewModelFactory (private val repository: UserRepo) : ViewModelProvider.NewInstanceFactory(){

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Suppress("UNCHECKED_CAST")
class EventFragmentVMFactory (private val repository: UserRepo, private val repoEvent: EventRepo) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventFragmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventFragmentViewModel(repository, repoEvent) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Suppress("UNCHECKED_CAST")
class HomeFragmentVMFactory(private val repository: UserRepo) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeFragmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeFragmentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class NotifFragmentVMFactory(private val repository: UserRepo, private val repoEvent: EventRepo) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotifFragmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotifFragmentViewModel(repository, repoEvent) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}