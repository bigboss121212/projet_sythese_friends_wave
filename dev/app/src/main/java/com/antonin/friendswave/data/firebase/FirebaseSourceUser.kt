package com.antonin.friendswave.data.firebase

import android.content.Context
import android.net.Uri
import com.antonin.friendswave.data.model.Event
import com.antonin.friendswave.data.model.Messages
import com.antonin.friendswave.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import io.reactivex.Completable
import java.util.Calendar

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: C'est le lien entre la base de donnée Firebase et notre application, elle recupere ou enregistre des données. Elle est dédiée en majorité aux users.

//Documentation sur les requetes https://firebase.google.com/docs/reference/kotlin/com/google/firebase/database/Query
// Beaucoup de temps passé à essayer de faire marcher l'asynchrone avec nos requêtes, il a fallut beaucoup d'essai et de remaniements grâce en partie à des forums
// mais aussi chatGpt pour des cas plus complexes ou sans solution.

class FirebaseSourceUser {

    var storage: FirebaseStorage = Firebase.storage

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    val firebaseData : DatabaseReference = FirebaseDatabase.getInstance().reference
    val mainUid = FirebaseAuth.getInstance().currentUser?.uid
    val userRef = firebaseData.child("user")

    fun currentUser() = firebaseAuth.currentUser

    fun logout() {
        firebaseAuth.signOut()
        FirebaseDatabase.getInstance().goOffline()

    }

    fun login(email: String, password: String) = Completable.create { emitter ->
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (!emitter.isDisposed) {
                if (it.isSuccessful)
                    emitter.onComplete()
                else
                    emitter.onError(it.exception!!)
            }
        }
    }

    fun register(name: String, email: String, password: String, familyName: String, nickname: String,
        city: String, date: String) = Completable.create { emitter ->
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            addUserToDatabase(name,email, firebaseAuth.currentUser?.uid!!, familyName, nickname, city, date)
            if (!emitter.isDisposed) {
                if (it.isSuccessful) {
                    emitter.onComplete()
                } else {
                    emitter.onError(it.exception!!)
                }
            }

        }
    }

    fun addUserToDatabase(name: String, email: String, uid: String, familyName: String,
        nickname: String, city: String, date: String){
        userRef.child(uid).setValue(User(name,email,uid, familyName, nickname, city, date))
    }

    fun getUserData(onResult: (User?) -> Unit) {
        userRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(User::class.java)
                    onResult(user)
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    onResult(null)
                }
            })
    }

    fun fetchInteret(onResult: (List<String>?) -> Unit) {
        firebaseData.child("interet")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val interetList = ArrayList<String>()
                    for(snap in snapshot.children){
                        val interet = snap.getValue(String::class.java)!!
                        interetList.add(interet)
                    }
                    onResult(interetList)
                }
                override fun onCancelled(error: DatabaseError) {
                    onResult(null)
                }
            })
    }

    //retourn la liste des pseudos de la db pour verification
    fun fetchAllPseudo(onResult: (List<String>) -> Unit){
        val pseudoList = ArrayList<String>()

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children){
                    val user = postSnapshot.getValue(User::class.java)
                    pseudoList.add(user?.nickname!!)
                }
                onResult(pseudoList)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    //pour update les donnes du main user
    fun editProfil(user_live: User?) {
        userRef.child(mainUid!!).setValue(user_live)
    }

    //a changer exactement similaire a fetch admin
    fun getUserProfilData(profilUid: String?, onResult: (User?) -> Unit){
        userRef.child(profilUid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(User::class.java)
                    onResult(user)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    onResult(null)
                }
            })
    }

    fun verifAmitier(profilUid: String?, onResult: (Boolean?) -> Unit){
        userRef.child(firebaseAuth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val mainUser = snapshot.getValue(User::class.java)!!
                        if (mainUser.friendList!!.containsKey(profilUid)) {
                            val ami = true
                            onResult(ami)
                        }
                        else{
                            val ami = false
                            onResult(ami)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    onResult(null)
                }
            })
    }

    fun fetchUsersFriend(onResult: (List<User>) -> Unit){
        val userList = ArrayList<User>()
        val mainUid = firebaseAuth.currentUser?.uid
        userRef.child(mainUid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val mainUser = snapshot.getValue(User::class.java)!!
                        userRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (postSnapshot in snapshot.children){
                                    val user = postSnapshot.getValue(User::class.java)
                                    if(mainUser.friendList!!.containsKey(user?.uid!!)){
                                        userList.add(user)
                                    }
                                }
                                onResult(userList)
                            }
                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    fun addFriendRequestToUser(email: String) {
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children) {
                    val currentUser = postSnapshot.getValue(User::class.java)
                    if (email == currentUser?.email) {
                        userRef.child(currentUser.uid!!).child("friendRequest").child(firebaseAuth.currentUser?.uid!!).setValue(firebaseAuth.currentUser?.email)
                        return
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    //verification si la requete d'amitier a deja ete envoye
    fun requestAlreadySend(email: String, onResult: (Boolean) -> Unit){
//        val userRef = firebaseData.child("user/")
        var requete = false
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children) {
                    val currentUser = postSnapshot.getValue(User::class.java)
                    if (email == currentUser?.email) {
                        if(currentUser.friendRequest!!.containsKey(mainUid!!)){
                            onResult(true)
                            requete = true
                        }
                    }
                }
                if (!requete)onResult(false)
            }
            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun addFriendRequestToUserByUid(uid: String?) {
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children) {
                    val currentUser = postSnapshot.getValue(User::class.java)
                    if (uid == currentUser?.uid) {
                        userRef.child(currentUser?.uid!!).child("friendRequest").child(firebaseAuth.currentUser?.uid!!).setValue(firebaseAuth.currentUser?.email)
                        return
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun removeFriend(uid: String?){
        userRef.child(mainUid!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val currentUser = dataSnapshot.getValue(User::class.java)
                    currentUser?.friendList?.remove(uid)
                    currentUser?.friends = currentUser?.friends?.minus(1)
                    userRef.child(mainUid).setValue(currentUser)
                    userRef.child(uid!!).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                val currentUser2 = dataSnapshot.getValue(User::class.java)
                                currentUser2?.friendList?.remove(mainUid)
                                currentUser2?.friends = currentUser2?.friends?.minus(1)
                                userRef.child(uid).setValue(currentUser2)
                            }
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

    fun sendSignalement(uid: String?, messSignalement: String?){
        val messageObject = Messages(messSignalement, mainUid)
        firebaseData.child("signalement").child(uid!!).child("message").push()
            .setValue(messageObject)
    }

    fun fetchUsersRequest(onResult: (List<User>) -> Unit){
        val mainUid = FirebaseAuth.getInstance().currentUser?.uid

        userRef.child(mainUid!!).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val mainUser = snapshot.getValue(User::class.java)!!
                    userRef.addValueEventListener(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val userList = ArrayList<User>()
                            for (postSnapshot in snapshot.children){
                                val currentUser = postSnapshot.getValue(User::class.java)
                                if(mainUser.friendRequest!!.containsKey(currentUser?.uid)){
                                    if(currentUser?.uid != mainUid){
                                        userList.add(currentUser!!)
                                    }
                                }
                            }
                            onResult(userList)
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

    fun acceptRequestUpdateUser(userNotif: User?){
        val userRef = FirebaseDatabase.getInstance().getReference("user").child(mainUid!!)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val mainUser = dataSnapshot.getValue(User::class.java)
                mainUser?.friendRequest!!.remove(userNotif?.uid)
                mainUser.friendList!!.put(userNotif?.uid!!, userNotif.email!!)
                mainUser.friends = mainUser.friends?.plus(1)
                firebaseData.child("user").child(mainUid).setValue(mainUser)
                firebaseData.child("user").child(userNotif.uid!!).child("friendList").child(mainUid).setValue(mainUser.email)
                firebaseData.child("user").child(userNotif.uid!!).child("friends").setValue(userNotif.friendList?.size?.plus(1))
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

    }

    fun refuseRequest(userNotif: User?){
        val userRef = FirebaseDatabase.getInstance().getReference("user").child(mainUid!!)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val mainUser = dataSnapshot.getValue(User::class.java)
                if(mainUser?.friendRequest!!.isNotEmpty()){
                    mainUser.friendRequest!!.remove(userNotif?.uid!!)
                    firebaseData.child("user").child(mainUid).setValue(mainUser)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    fun addMessagetoDatabase(
        messageEnvoye: String,
        receiverUid: String,
        user: User?,
        formattedTimestamp: String
    ){
        val messageObject = Messages(messageEnvoye, mainUid, user?.name, formattedTimestamp)
        val senderRoom = receiverUid + mainUid
        val receiverRoom = mainUid + receiverUid

        firebaseData.child("chats").child(senderRoom).child("message").push()
            .setValue(messageObject).addOnSuccessListener {
                firebaseData.child("chats").child(receiverRoom).child("message").push()
                    .setValue(messageObject)
            }
    }

    fun addMessageGrouptoDatabase(
        messageEnvoye: String,
        receiverUid: String,
        userName: String,
        formattedTimestamp: String
    ){
        val messageObject = Messages(messageEnvoye, mainUid, userName, formattedTimestamp)

        firebaseData.child("chats").child(receiverUid).child("message").push()
            .setValue(messageObject)
    }

    fun fetchDiscussion(receiverUid: String, onResult:(List<Messages>) -> Unit){
        val senderRoom = receiverUid + mainUid

        firebaseData.child("chats").child(senderRoom).child("message").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val messageList = ArrayList<Messages>()
                for (snap in task.result.children) {
                    if (snap.exists()) {
                        val message = snap.getValue(Messages::class.java)
                        messageList.add(message!!)

                    }
                }
                onResult(messageList)
            }
        }
    }

    fun fetchDiscussionGroup(receiverUid: String, onResult:(List<Messages>) -> Unit){
        firebaseData.child("chats").child(receiverUid).child("message").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val messageList = ArrayList<Messages>()
                for (snap in task.result.children) {
                    if (snap.exists()) {
                        val message = snap.getValue(Messages::class.java)
                        messageList.add(message!!)

                    }
                }
                onResult(messageList)
            }
        }
    }

    fun fetchParticipant(event: Event?, onResult:(List<User>) -> Unit){

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = ArrayList<User>()
                for (postSnapshot in snapshot.children){
                    val user = postSnapshot.getValue(User::class.java)
                    if(event!!.listInscrits.containsValue(user!!.email) || event.admin == user.uid){
                        userList.add(user)
                    }
                }
                onResult(userList)
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    fun fetchEmail(onResult: (List<String>) -> Unit){
//        val userRef = firebaseData.child("user/")

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val emailList = ArrayList<String>()
                for (postSnapshot in snapshot.children){
                    val user = postSnapshot.getValue(User::class.java)
                    emailList.add(user!!.email.toString())
                }
                onResult(emailList)
            }
            override fun onCancelled(error: DatabaseError) {


            }
        })
    }

    fun fetchAllUser(onResult: (List<User>) -> Unit){
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = ArrayList<User>()
                for (postSnapshot in snapshot.children){
                    val user = postSnapshot.getValue(User::class.java)

                    userList.add(user!!)

                }
                onResult(userList)
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    fun registerPhoto(photo: Uri, context: Context, path : String) : String{

        val currentTime = Calendar.getInstance().timeInMillis
        val storageRef = storage.reference.child(path).child(mainUid!!).child(currentTime.toString())
        val path_img = storageRef.toString().substringAfter(path)
        val inputStream = context.contentResolver.openInputStream(photo)
        val uploadTask = storageRef.putStream(inputStream!!)
        uploadTask.addOnSuccessListener {
        }.addOnFailureListener {
            // Une erreur s'est produite lors du chargement de la photo
        }
        return path_img
    }

    fun registerPhotoCover(photo: Uri, context: Context) : String{

        val currentTime = Calendar.getInstance().timeInMillis
        val storageRef = storage.reference.child("photosCover/").child(mainUid!!).child(currentTime.toString())
        val path_img = storageRef.toString().substringAfter("photosCover/")
        val inputStream = context.contentResolver.openInputStream(photo)
        val uploadTask = storageRef.putStream(inputStream!!)
        uploadTask.addOnSuccessListener {
        }.addOnFailureListener {
            // Une erreur s'est produite lors du chargement de la photo
        }
        return path_img
    }


}