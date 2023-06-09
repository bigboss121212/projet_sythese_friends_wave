package com.antonin.friendswave.data.repository

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.antonin.friendswave.data.firebase.FirebaseSourceUser
import com.antonin.friendswave.data.model.Event
import com.antonin.friendswave.data.model.Messages
import com.antonin.friendswave.data.model.User


//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: C'est le pont entre FirebaseSource et le View Model, ici c'est les utilisateurs

class UserRepo( private val firebaseUser: FirebaseSourceUser) {

    fun currentUser() = firebaseUser.currentUser()

    fun register(
        name: String,
        email: String,
        password: String,
        familyName: String,
        nickname: String,
        city: String,
        date: String
    ) = firebaseUser.register(name,email, password, familyName, nickname, city, date)

    fun login(email: String, password: String) = firebaseUser.login(email, password)

    fun logout() = firebaseUser.logout()


    fun fetchAllPseudo():LiveData<List<String>> {
        val pseudoList = MutableLiveData<List<String>>()

        firebaseUser.fetchAllPseudo { pseudo ->
            pseudoList.postValue(pseudo)
        }
        return pseudoList
    }

    fun fetchUsersFriends():LiveData<List<User>> {
        val emailUserList = MutableLiveData<List<User>>()

        firebaseUser.fetchUsersFriend { user ->
            emailUserList.postValue(user)
        }
        return emailUserList
    }


    fun fetchUsersRequest(): LiveData<List<User>> {
        val notifUserList = MutableLiveData<List<User>>()

        firebaseUser.fetchUsersRequest { notifUser ->
            notifUserList.postValue(notifUser)
        }
        return notifUserList
    }

    fun addFriendRequestToUser(email: String) = firebaseUser.addFriendRequestToUser(email)


    fun requestAlreadySend(email: String):LiveData<Boolean>{
        val doubleRequest = MutableLiveData<Boolean>()
        firebaseUser.requestAlreadySend(email) { bool ->
            doubleRequest.postValue(bool)
        }
        doubleRequest.value
        return doubleRequest
    }


    fun addFriendRequestToUserByUid(uid: String?) = firebaseUser.addFriendRequestToUserByUid(uid)

    fun removeFriend(uid: String?) = firebaseUser.removeFriend(uid)

    fun sendSignalement(uid: String?, messSignalement: String?)  = firebaseUser.sendSignalement(uid,  messSignalement)

    fun getUserData(): LiveData<User> {
        val userLiveData = MutableLiveData<User>()

        firebaseUser.getUserData { user ->
            userLiveData.postValue(user)
        }

        return userLiveData
    }

    fun fetchAdmin(uid:String): LiveData<User> {
        val adminLiveData = MutableLiveData<User>()

        firebaseUser.getUserProfilData(uid) { user ->
            adminLiveData.postValue(user)
        }

        return adminLiveData
    }

    fun fetchInteret(): LiveData<List<String>?> {
        val interetLiveData = MutableLiveData<List<String>?>()

        firebaseUser.fetchInteret { interet ->
            interetLiveData.postValue(interet)
        }

        return interetLiveData
    }

    fun editProfil(user_live: User?) = firebaseUser.editProfil(user_live)

    fun getUserProfilData(profilUid: String?): LiveData<User> {
        val userLiveData = MutableLiveData<User>()

        firebaseUser.getUserProfilData(profilUid) { user ->
            userLiveData.postValue(user)
        }

        return userLiveData
    }

    fun verifAmitier(profilUid: String?): LiveData<Boolean> {
        val amiLiveData = MutableLiveData<Boolean>()

        firebaseUser.verifAmitier(profilUid) { ami ->
            amiLiveData.postValue(ami)
        }

        return amiLiveData
    }

    fun acceptRequest1(userNotif: User?){
        firebaseUser.acceptRequestUpdateUser(userNotif)
    }

    fun refuseRequest(userNotif: User?){
        firebaseUser.refuseRequest(userNotif)
    }

    fun fetchDiscussion(receiverUid: String):LiveData<List<Messages>>{
        val messageList = MutableLiveData<List<Messages>>()

        firebaseUser.fetchDiscussion(receiverUid) { message ->
            messageList.postValue(message)
        }
        return messageList
    }

    fun fetchDiscussionGroup(receiverUid: String):LiveData<List<Messages>>{
        val messageList = MutableLiveData<List<Messages>>()

        firebaseUser.fetchDiscussionGroup(receiverUid) { message ->
            messageList.postValue(message)
        }
        return messageList
    }

    fun fetchParticipant(event: Event?):LiveData<List<User>>{
        val participant = MutableLiveData<List<User>>()

        event?.listInscrits

        firebaseUser.fetchParticipant(event) { participants ->
            participant.postValue(participants)
        }
        return participant
    }

    fun fetchfetchEmail():LiveData<List<String>>{
        val emailList = MutableLiveData<List<String>>()

        firebaseUser.fetchEmail { message ->
            emailList.postValue(message)
        }
        return emailList
    }

    fun addMessagetoDatabase(
        messageEnvoye: String,
        receiverUid: String,
        user: User?,
        formattedTimestamp: String
    ){
        firebaseUser.addMessagetoDatabase(messageEnvoye, receiverUid, user, formattedTimestamp)
    }

    fun addMessageGrouptoDatabase(
        messageEnvoye: String,
        receiverUid: String,
        userName: String,
        formattedTimestamp: String
    ){
        firebaseUser.addMessageGrouptoDatabase(messageEnvoye, receiverUid, userName, formattedTimestamp)
    }


    fun fetchAllUser():LiveData<List<User>>{
        val userList = MutableLiveData<List<User>>()
        firebaseUser.fetchAllUser { user ->
            userList.postValue(user)
        }
        return userList
    }

    fun registerPhoto(photo: Uri, context: Context, path:String): String{

        return firebaseUser.registerPhoto(photo, context, path)
    }



}