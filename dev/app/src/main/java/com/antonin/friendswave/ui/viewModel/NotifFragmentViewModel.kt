package com.antonin.friendswave.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.antonin.friendswave.data.model.Event
import com.antonin.friendswave.data.model.User
import com.antonin.friendswave.data.repository.EventRepo
import com.antonin.friendswave.data.repository.UserRepo

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Classe permettant de gerer le ViewModel pour lees notifs de la page d'accueil

class NotifFragmentViewModel (private val repository: UserRepo, private val repoEvent:EventRepo): ViewModel() {

    private val _eventList = MutableLiveData <List<Event>>()
    val eventList: LiveData<List<Event>> = _eventList

    private val _friendNotifList = MutableLiveData <List<User>>()
    val friendNotifList: LiveData<List<User>> = _friendNotifList

    private val _requestListEvent = MutableLiveData<List<User>>()
    val requestListEvent : LiveData<List<User>> = _requestListEvent



    fun fetchUsersRequest(){
        repository.fetchUsersRequest().observeForever{ notifUser ->
            _friendNotifList.value = notifUser
        }
    }

    //pour les notifs p-e faire un NotifFragementViewModel
    fun acceptRequest(userNotif: User?){
        repository.acceptRequest1(userNotif)

    }

    fun refuseRequest(userNotif: User?){
        repository.refuseRequest(userNotif)
    }

    fun refuseInvitationEvent(event:Event?){
        repoEvent.refuseInvitationEvent(event)
    }

    fun acceptInvitationEvent(event:Event?){
        repoEvent.acceptInvitationEvent(event)
    }

    fun acceptRequestEvent(user:User?){

        repoEvent.acceptRequestEvent(user)
    }


    fun declineRequestEvent(user:User?){

        repoEvent.declineRequestEvent(user)
    }

    fun fetchEventsInvitation() {
        repoEvent.fetchInvitationEvents().observeForever { event ->
            _eventList.value = event
        }
    }

    fun  fetchDemandeInscriptionEventPublic(){

        repoEvent.fetchDemandeInscriptionEventPublic().observeForever { user->
            _requestListEvent.value = user


        }
    }
}