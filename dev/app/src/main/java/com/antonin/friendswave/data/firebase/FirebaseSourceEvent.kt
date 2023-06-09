package com.antonin.friendswave.data.firebase

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import com.antonin.friendswave.data.model.Event
import com.antonin.friendswave.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: C'est le lien entre la base de donnée Firebase et notre application, elle recupere ou enregistre des données. Elle est dédiée en majorité aux évents.

//Documentation sur les requetes https://firebase.google.com/docs/reference/kotlin/com/google/firebase/database/Query
// Beaucoup de temps passé à essayer de faire marcher l'asynchrone avec nos requêtes, il a fallut beaucoup d'essai et de remaniements grâce en partie à des forums
// mais aussi chatGpt pour des cas plus complexes ou sans solution.

class FirebaseSourceEvent {

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    val firebaseData : DatabaseReference = FirebaseDatabase.getInstance().reference
    val mainUid = FirebaseAuth.getInstance().currentUser?.uid
    fun currentUser() = firebaseAuth.currentUser
    val firebaseEvent = firebaseData.child("event")
    val firebaseUserCurrent = firebaseData.child("user").child(mainUid!!)

    fun editEvent(event: Event?){
        firebaseEvent.child(event!!.key.toString()).setValue(event)
    }

    fun deleteEvent(event: Event?){
        firebaseEvent.child(event!!.key.toString()).removeValue()
        firebaseData.child("chatsGroup").child(mainUid!!+ event.key.toString()).removeValue()
    }

    fun deleteConfirmation(event:Event?){
        firebaseEvent.child(event!!.key.toString()).child("listInscrits").child(mainUid!!).removeValue().addOnSuccessListener {
            firebaseData.child("user").child(mainUid).child("eventConfirmationList").child(event.key.toString()).removeValue()
        }
    }

    fun deleteConfirmationGuest(event: Event?, idGuest: String){
        firebaseEvent.child(event!!.key.toString()).child("listInscrits").child(idGuest).removeValue().addOnSuccessListener {
            firebaseData.child("user").child(idGuest).child("eventConfirmationList").child(event.key.toString()).removeValue()
        }
    }

    fun deletePendingEvent(event:Event?){
        firebaseEvent.child(event!!.key.toString()).child("pendingRequestEventPublic").child(currentUser()!!.uid).removeValue()
        firebaseUserCurrent.child("pendingRequestEventPublic").child(event.key.toString()).removeValue()
    }

    // GET EVENT DATA DETAIL PUBLIC PAGE EVENT FRAGMENT MAIN :
    fun getEventData(key:String,onResult: (Event?) -> Unit) {
        firebaseEvent.child(key).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(Event::class.java)!!
                onResult(data)
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(null)
            }
        })
    }

    fun sendRequestToParticipatePublicEvent(idEvent:String, adminEvent: String){

        firebaseEvent.child(idEvent).child("pendingRequestEventPublic").child(currentUser()!!.uid).setValue(currentUser()!!.email)
        firebaseUserCurrent.child("pendingRequestEventPublic").child(idEvent).setValue(adminEvent)

    }

    fun sendAnInvitationEvent(event:Event, email:String){
        firebaseData.child("user").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (childSnapshot in dataSnapshot.children) {
                        val user = childSnapshot.getValue(User::class.java)
                        if(user!!.email == email){
                            val guestId = childSnapshot.key
                            val childUpdates = HashMap<String, Any>()
                            childUpdates["/user/$guestId/invitations/${event.key}"] = mainUid!!

                            firebaseData.updateChildren(childUpdates)
                            firebaseEvent.child(event.key.toString()).child("invitation").child(guestId!!).setValue(email)
                        }else {
                            println("Aucun utilisateur trouvé avec cette adresse e-mail")
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    // POUR ENVOYER DES NOTIFS de demande de participation  A L'USER QUI A FAIT UN EVENT
    fun fetchDemandeInscriptionEventPublic( onResult: (List<User>) -> Unit) {

        val userList: ArrayList<String> = ArrayList()
        val tempList: ArrayList<User> = ArrayList()

        firebaseEvent.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                for (data in snapshot.children){
                    val event = data.getValue(Event::class.java)
                    if(event!!.admin == mainUid){
                        for (i in event.pendingRequestEventPublic ){
                            userList.add(i.key)
                        }
                    }
                }

                    firebaseData.child("user").addValueEventListener(object:ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {

                            while (userList.isNotEmpty()) {
                                for(snap in snapshot.children) {
                                    val user = snap.getValue(User::class.java)
                                    if(userList.contains(user!!.uid)){
                                        tempList.add(user)
                                        userList.remove(user.uid)
                                    }
                                }
                            }
                            onResult(tempList)
                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                    })

                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }



    fun getAllEventsPendingRequestPublic(onResult: (List<Event>) -> Unit){

        var hostId: Any?
        var eventValue: Any?
        val eventIdList = HashMap<String,String>()
        val eventList: ArrayList<Event> = ArrayList()
        firebaseUserCurrent.child("pendingRequestEventPublic").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (data in dataSnapshot.children){
                        hostId = data.value.toString()
                        eventValue = data.key.toString()
                        eventIdList.put(eventValue.toString(),hostId.toString())

                    }
                    searchEventsWithKey(eventIdList,eventList, onResult)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun fetchInvitationEvents( onResult: (List<Event>) -> Unit) {

        var eventId: Any?
        var eventValue: Any?
        val eventIdList = HashMap<String,String>()
        val eventList: ArrayList<Event> = ArrayList()
         firebaseUserCurrent.child("invitations").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (data in dataSnapshot.children){
                        eventValue = data.value.toString()
                        eventId = data.key.toString()
                        eventIdList.put(eventId.toString(),eventValue.toString())

                    }
                    searchEventsWithKey(eventIdList,eventList, onResult)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun fetchConfirmationEvents( onResult: (List<Event>) -> Unit) {
        var hostId: Any?
        var eventValue: Any?
        val eventIdList = HashMap<String,String>()
        val eventList: ArrayList<Event> = ArrayList()
        firebaseUserCurrent.child("eventConfirmationList").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (data in dataSnapshot.children){
                        hostId = data.value.toString()
                        eventValue = data.key.toString()
                        eventIdList.put(eventValue.toString(),hostId.toString())
                    }
                    searchEventsWithKey(eventIdList,eventList,onResult)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun searchEventsWithKey(eventIdList: HashMap<String, String>, eventList: ArrayList<Event>, onResult: (List<Event>) -> Unit){
        firebaseEvent.addValueEventListener(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children) {
                    val event = snap.getValue(Event::class.java)
                    if(eventIdList.containsKey(event!!.key)){
                        eventList.add(event)
                    }
                }
                onResult(eventList)
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun fetchSpecificEvents( eventValue: String, onResult: (Event) -> Unit) {
        val eventValue = eventValue
        firebaseEvent.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                    for (snap in task.result.children) {
                        if (snap.exists()) {
                            if(eventValue == snap.key){
                                val event = snap.getValue(Event::class.java)!!
                                onResult(event)
                            }
                        }
                    }
                }
            }
    }


    fun fetchGuestConfirmDetailEventPublic(key:String?, onResult: (List<User>) -> Unit){

        val listGuest : ArrayList<String> = ArrayList()

        firebaseEvent.child(key!!).child("listInscrits").addValueEventListener( object :ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for(data in dataSnapshot.children){
                            listGuest.add(data!!.value.toString())
                        }
                        searchGuest(listGuest, onResult)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    @SuppressLint("SuspiciousIndentation")
    fun fetchGuestDetailEventPublic(key:String?, onResult: (List<User>) -> Unit){

        val listGuest : ArrayList<String> = ArrayList()

        firebaseEvent.child(key!!).child("invitation").addValueEventListener( object :ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for(data in dataSnapshot.children){
                        listGuest.add(data!!.value.toString())
                    }
                    searchGuest(listGuest, onResult)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun fetchPendingGuestEventPublic(key:String?, onResult: (List<User>) -> Unit){

        val listGuest : ArrayList<String> = ArrayList()

        firebaseEvent.child(key!!).child("pendingRequestEventPublic").addValueEventListener( object :ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for(data in dataSnapshot.children){
                        listGuest.add(data!!.value.toString())
                    }
                    searchGuest(listGuest, onResult)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun searchGuest(listGuest:ArrayList<String>, onResult: (List<User>) -> Unit){
        firebaseData.child("user/").addValueEventListener( object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList:ArrayList<User> = ArrayList()
                for(data in snapshot.children){
                    val user = data.getValue(User::class.java)
                    if(listGuest.contains(user!!.email)){
                        userList.add(user)
                    }
                }
                onResult(userList)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun getKeyFromValue(map: HashMap<String, String>, value: String): String? {
        for ((key, mapValue) in map.entries) {
            if (mapValue == value) {
                return key
            }
        }
        return null
    }
    // Un énième essai que nous avons fait avec l'aide de chatGPT afin d'avoir une coroutine sur l'acceptation d'un event. Helas, nous ne sommes pas parvenus et nous avons
    // cherché de longues heures sans trouver de solutions
    fun acceptRequestEvent(user: User?) {
        // Utilisation d'une coroutine pour effectuer des opérations asynchrones
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Effectuer la requête de suppression de la demande de l'utilisateur dans l'événement
                withContext(Dispatchers.IO) {
                    firebaseEvent.child(getKeyFromValue(user!!.pendingRequestEventPublic!!, mainUid!!).toString())
                        .child("pendingRequestEventPublic")
                        .child(user.uid.toString())
                        .removeValue()
                }

                // Effectuer la requête d'ajout de l'utilisateur dans la liste des inscrits de l'événement
                withContext(Dispatchers.IO) {
                    firebaseEvent.child(getKeyFromValue(user!!.pendingRequestEventPublic!!, mainUid!!).toString())
                        .child("listInscrits")
                        .child(user.uid.toString())
                        .setValue(user.email.toString())
                }

                // Effectuer la requête de suppression de la demande d'événement chez l'utilisateur
                withContext(Dispatchers.IO) {
                    firebaseData.child("user")
                        .child(user!!.uid.toString())
                        .child("pendingRequestEventPublic")
                        .child(getKeyFromValue(user.pendingRequestEventPublic!!, mainUid!!).toString())
                        .removeValue()
                }

                // Effectuer la requête d'ajout de l'événement dans la liste de confirmation de l'utilisateur
                withContext(Dispatchers.IO) {
                    firebaseData.child("user")
                        .child(user!!.uid.toString())
                        .child("eventConfirmationList")
                        .child(getKeyFromValue(user.pendingRequestEventPublic!!, mainUid!!).toString())
                        .setValue(mainUid)
                }

                // Effectuer la requête d'ajout de l'utilisateur dans la liste de confirmation de l'hôte
                withContext(Dispatchers.IO) {
                    firebaseData.child("user")
                        .child(mainUid!!)
                        .child("confirmHostRequestEventPublic")
                        .child(getKeyFromValue(user!!.pendingRequestEventPublic!!, mainUid).toString())
                        .setValue(user.email)
                }

                // Mettre à jour le RecyclerView ici ou émettre un événement pour notifier les changements

            } catch (e: Exception) {
                // Gérer les erreurs
            }
        }
    }



    // REFUSER
    fun declineRequestEvent(user: User?){
        val idEvent:String = user!!.pendingRequestEventPublic!!.get(mainUid!!).toString()
        val queryEventPublic = firebaseEvent.child(idEvent)
        val queryAcceptGuestEventUser = firebaseData.child("user").child(user.uid.toString())

        queryEventPublic.child("pendingRequestEventPublic").child(user.uid.toString()).removeValue()
        queryAcceptGuestEventUser.child("pendingRequestEventPublic").child(idEvent).removeValue()

    }

    fun acceptInvitationEvent(event: Event?){

        val queryEvent = firebaseEvent.child(event!!.key.toString())
        val queryAcceptEventUser = firebaseData.child("user").child(mainUid!!)

        queryEvent.child("invitation").child(mainUid).removeValue()
        queryEvent.child("listInscrits").child(mainUid).setValue(currentUser()!!.email.toString())

        queryAcceptEventUser.child("invitations").child(event.key!!).removeValue()
        queryAcceptEventUser.child("eventConfirmationList").child(event.key!!).setValue(event.admin)

    }

    fun refuseInvitationEvent(event: Event?){
        val queryEvent = firebaseEvent.child(event!!.key.toString())
        val queryAcceptEventUser = firebaseData.child("user").child(mainUid!!)

        queryEvent.child("invitation").child(mainUid).removeValue()
        queryAcceptEventUser.child("invitations").child(event.key!!).removeValue()
    }


    // Recupere les events publics dans Event Fragment :
    fun fetchEvents(onResult: (List<Event>) -> Unit){

        firebaseEvent.addValueEventListener(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val eventsList = ArrayList<Event>()
                for (snap in snapshot.children) {
                    val event = snap.getValue(Event::class.java)
                    if(event!!.public == true){
                        eventsList.add(event)
                    }
                }
                onResult(eventsList)
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    // Recupere les events Privées de l'user et les insere dans My Event Fragment :
    fun fetchEventsPrivateUser(onResult: (List<Event>) -> Unit) {
        firebaseEvent.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val eventsList = ArrayList<Event>()
                for (snap in task.result.children) {
                    if (snap.exists()) {
                        val event = snap.getValue(Event::class.java)
                        if(event!!.admin == mainUid && event.public == false)
                            eventsList.add(event)
                    }
                }
                onResult(eventsList)
            }
            else {
                task.exception?.printStackTrace()
            }
        }
    }

    // Recupere les events Publics de l'user et les insere dans My Event Fragment :
    fun fetchEventsPublicUser(onResult: (List<Event>) -> Unit) {
        firebaseEvent.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val eventsList = ArrayList<Event>()
                for (snap in task.result.children) {
                    if (snap.exists()) {
                        val event = snap.getValue(Event::class.java)
                        if(event!!.admin == mainUid && event.public == true)
                            eventsList.add(event)
                    }
                }
                onResult(eventsList)
            }
        }
    }

    // ON RAMENE JUSTE LEVENT en DETAIL pour MY EVENT
    fun fetchDetailEventPublicUser(key: String?, onResult: (Event?) -> Unit) {

        firebaseData.child("event/").child(key!!).addListenerForSingleValueEvent(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val event = snapshot.getValue(Event::class.java)
                onResult(event!!)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun addEventUser(name: String, isPublic: Boolean, nbrePersonnes:Int, uid: String, category:String, date: String, horaire:String, adress:String,
        description:String, longitude: String, latitude: String, photo: Uri, context: Context, host:String, timeStamp:Double, duree:Int)
    {
        val database = Firebase.database
        val myRef = database.getReference("event").push()
        myRef.setValue(Event(myRef.key,name,isPublic,nbrePersonnes, uid, category, date, horaire,
            adress, description, latitude, longitude, duree, host,timeStamp, nbreInscrit = 0))
        registerPhotoEvent(photo, context, myRef.key!!)

    }

    fun registerPhotoEvent(photo: Uri,context:Context ,key:String) : String{

        val storage: FirebaseStorage = Firebase.storage
        val currentTime = Calendar.getInstance().timeInMillis
        val storageRef = storage.reference.child("photosEvent/").child(key).child(currentTime.toString())
        val path = storageRef.toString().substringAfter("photosEvent/")
        Firebase.database.getReference("event/" + key).child("imgEvent").setValue(path)
        val inputStream = context.contentResolver.openInputStream(photo)
        val uploadTask = storageRef.putStream(inputStream!!)
        uploadTask.addOnSuccessListener {

        }.addOnFailureListener {

        }

        return path
    }
}