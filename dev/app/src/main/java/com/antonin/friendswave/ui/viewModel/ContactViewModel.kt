package com.antonin.friendswave.ui.viewModel

import android.content.DialogInterface
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.antonin.friendswave.data.repository.UserRepo
import com.antonin.friendswave.data.model.User
import com.antonin.friendswave.outils.AlertDialog
import com.antonin.friendswave.outils.emailRegex
import com.antonin.friendswave.outils.sendEmail
import com.antonin.friendswave.outils.toastShow
import com.antonin.friendswave.ui.authentification.InterfaceAuth

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Classe permettant de gerer le ViewModel pour les contacts

class ContactViewModel(private val repository: UserRepo) : ViewModel() {

    var email: MutableLiveData<String?>  = MutableLiveData()
    var interfaceAuth : InterfaceAuth? = null
    val user by lazy {
        repository.currentUser()
    }

    private val _user = MutableLiveData<User>()
    var user_live: LiveData<User> = _user

    private val _requete = MutableLiveData<Boolean>()
    var requete_live: LiveData<Boolean> = _requete

    private val _totalUserList = MutableLiveData<List<User>>()
    val totalUserList: LiveData<List<User>> = _totalUserList

    private val _emailList = MutableLiveData<List<String>>()
    val emailList: LiveData<List<String>> = _emailList

    fun fetchEmail() {
        repository.fetchfetchEmail().observeForever{ email ->
            _emailList.value = email
        }
    }

    fun verifRequete(){

        repository.requestAlreadySend(email.value.toString()).observeForever{ user ->
            _requete.value = user
        }

    }

    fun addFriendRequestToUser(view: View){
        val alertDialog = AlertDialog()

        val positiveButtonClickListener = DialogInterface.OnClickListener { _, which ->
            // Code à exécuter si le bouton positif est cliqué
            if (which == DialogInterface.BUTTON_POSITIVE) {
                //envoyer une demande directement par couriel
                sendEmail(email.value.toString(),user_live.value!!.name!!, user_live.value!!.email!!)
                toastShow(view.context,"demande envoyée par courriel")
                alertDialog.cancel()
            }
        }
        val negativeButtonClickListener = DialogInterface.OnClickListener { _, which ->
            // Code à exécuter si le bouton négatif est cliqué
            if (which == DialogInterface.BUTTON_NEGATIVE) {
                toastShow(view.context,"ok on ne touche à rien")
                alertDialog.cancel()
            }
        }
        if (email.value.isNullOrEmpty()) {
            //faire un interface pour indiquer les erreurs
            interfaceAuth?.onFailure("Please enter a mail")
            return
        }
        if (!emailRegex.matches(email.value.toString())){
            interfaceAuth?.onFailure("Please enter a valid mail")
            return
        }
        if(!emailList.value!!.contains(email.value)){
//            interfaceAuth?.onFailure("This email match no account, we are sending an invitation via email")

            alertDialog.showDialog(view.context,
                "Attention",
                "This email match no account, do you want to make an invitation via email",
                "yes",
                "no",
                positiveButtonClickListener, negativeButtonClickListener)

            return
        }
        if (user_live.value!!.friendList!!.containsValue(email.toString())){
            interfaceAuth?.onFailure("Already in your contact")
            return
        }
        // verifier qu'une demande na pas deja ete envoye
        // A REVOIR on fait 2 verifications
//        if(requete){
//            interfaceAuth?.onFailure("Request already send")
//            return
//        }

        email.value =""

        toastShow(view.context,"Demande envoyée")

        repository.addFriendRequestToUser(email.toString())
    }

    fun fetchAllUser(){
        repository.fetchAllUser().observeForever{ user ->
            _totalUserList.value = user
        }
    }

    fun fetchUserData() {
        repository.getUserData().observeForever { user ->
            _user.value = user
        }
    }


}