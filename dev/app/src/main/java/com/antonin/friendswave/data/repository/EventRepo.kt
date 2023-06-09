package com.antonin.friendswave.data.repository

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.antonin.friendswave.data.firebase.FirebaseSourceEvent
import com.antonin.friendswave.data.model.Event
import com.antonin.friendswave.data.model.User


//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: C'est le pont entre FirebaseSource et le View Model, ici c'est les events

class EventRepo(private val firebaseEvent: FirebaseSourceEvent) {

    fun currentUser() = firebaseEvent.currentUser()

    fun fetchConfirmationEvents(): LiveData<List<Event>> {

        val eventListConfirm = MutableLiveData<List<Event>>()

        firebaseEvent.fetchConfirmationEvents { event ->
            eventListConfirm.postValue(event)
        }
        return eventListConfirm
    }

    fun fetchSpecificEvents( eventKey: String):LiveData<Event>{
        val event = MutableLiveData<Event>()
        firebaseEvent.fetchSpecificEvents(eventKey) { user ->
            event.postValue(user)
        }
        return event
    }

    fun fetchInvitationEvents() : LiveData<List<Event>> {

        val eventList = MutableLiveData<List<Event>>()

        firebaseEvent.fetchInvitationEvents { event ->
            eventList.postValue(event)
        }

        return eventList
    }

    fun refuseInvitationEvent(event:Event?){
        firebaseEvent.refuseInvitationEvent(event)
    }

    fun acceptInvitationEvent(event: Event?){
        firebaseEvent.acceptInvitationEvent(event)
    }

    fun acceptRequestEvent(user:User?){

        firebaseEvent.acceptRequestEvent(user)
    }

    fun declineRequestEvent(user:User?){

        firebaseEvent.declineRequestEvent(user)
    }

    fun fetchEvents() : LiveData<List<Event>> {

        val eventList = MutableLiveData<List<Event>>()

        firebaseEvent.fetchEvents { event ->
            eventList.postValue(event)
        }

        return eventList
    }

    fun addEventUser(name: String, isPublic: Boolean, nbrePersonnes:Int, uid: String, category:String, date: String, horaire:String, adress:String,
        description:String, longitude: String, latitude: String, photo:Uri, context: Context, host:String, timeStamp : Double, duree:Int) =
        firebaseEvent.addEventUser(name,isPublic,nbrePersonnes, uid, category, date, horaire, adress,description, longitude,latitude, photo, context, host, timeStamp, duree)

    fun editEvent(event:Event?)= firebaseEvent.editEvent(event)

    fun deleteEvent(event:Event?) = firebaseEvent.deleteEvent(event)
    fun deleteConfirmation(event: Event?) = firebaseEvent.deleteConfirmation(event)
    fun deleteConfirmationGuest(event: Event?, idGuest: String) = firebaseEvent.deleteConfirmationGuest(event, idGuest)

    fun deletePendingEvent(event: Event?) = firebaseEvent.deletePendingEvent(event)

    fun fetchEventsPrivateUser() : LiveData<List<Event>> {
        val eventList = MutableLiveData<List<Event>>()

        firebaseEvent.fetchEventsPrivateUser { event ->
            eventList.postValue(event)
        }
        return eventList
    }

    fun sendRequestToParticipatePublicEvent(idEvent:String, adminEvent:String){
        firebaseEvent.sendRequestToParticipatePublicEvent(idEvent,adminEvent)
    }

    fun getAllEventsPendingRequestPublic() : LiveData<List<Event>>{
        val pendingEvents = MutableLiveData<List<Event>>()

        firebaseEvent.getAllEventsPendingRequestPublic { event ->
            pendingEvents.postValue(event)
        }
        return pendingEvents
    }

    fun sendAnInvitationEvent(email: String, event: Event) = firebaseEvent.sendAnInvitationEvent(event,email)

    fun fetchEventsPublicUser() : LiveData<List<Event>> {
        val eventList = MutableLiveData<List<Event>>()

        firebaseEvent.fetchEventsPublicUser { event ->
            eventList.postValue(event)
        }
        return eventList
    }


    fun fetchGuestConfirmDetailEventPublic(key: String?): LiveData<List<User>> {
        val confirm_guest_list = MutableLiveData<List<User>>()

        firebaseEvent.fetchGuestConfirmDetailEventPublic(key) { user ->
            confirm_guest_list.postValue(user)
        }
        return confirm_guest_list
    }

    fun fetchGuestDetailEventPublic(key:String?): LiveData<List<User>> {
        val guestList = MutableLiveData<List<User>>()

        firebaseEvent.fetchGuestDetailEventPublic(key) { user ->
            guestList.postValue(user)
        }
        return guestList
    }

    fun fetchPendingGuestEventPublic(key:String?): LiveData<List<User>> {
        val guestList = MutableLiveData<List<User>>()

        firebaseEvent.fetchPendingGuestEventPublic(key) { user ->
            guestList.postValue(user)
        }
        return guestList
    }
    fun fetchDetailEventPublicUser(key:String?): LiveData<Event> {
        val eventPublicUser = MutableLiveData<Event>()

        firebaseEvent.fetchDetailEventPublicUser(key) { event ->
            eventPublicUser.postValue(event)
        }
        return eventPublicUser
    }

    fun fetchDemandeInscriptionEventPublic(): LiveData<List<User>> {
        val requestEvent = MutableLiveData<List<User>>()
        firebaseEvent.fetchDemandeInscriptionEventPublic { user ->
            requestEvent.postValue(user)

        }
        return requestEvent
    }

    fun getEventData(key: String) : LiveData<Event> {
        val eventLiveData = MutableLiveData<Event>()

        firebaseEvent.getEventData(key) { event ->
            eventLiveData.postValue(event)
        }
        return eventLiveData
    }
}