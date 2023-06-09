package com.antonin.friendswave.ui.viewModel

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.net.Uri
import android.view.View
import android.widget.AdapterView
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.antonin.friendswave.data.model.User
import com.antonin.friendswave.data.repository.UserRepo
import com.antonin.friendswave.outils.goToActivityWithArgs
import com.antonin.friendswave.outils.goToActivityWithoutArgs
import com.antonin.friendswave.outils.toastShow
import com.antonin.friendswave.strategy.*
import com.antonin.friendswave.ui.contact.AddContactActivity
import com.antonin.friendswave.ui.home.ManageHomeActivity
import com.antonin.friendswave.ui.home.SignalementActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Classe permettant de gerer le ViewModel pour la page d'accueil

//Concernant l'architecture MVVM https://www.simplifiedcoding.net/firebase-mvvm-example/
//Concernant le LiveData https://developer.android.com/topic/libraries/architecture/livedata?hl=fr

class HomeFragmentViewModel(private val repository: UserRepo):ViewModel() {

    var profilUid: String? = ""
    var messSignalement: String? = ""
    var day: Int? = 0
    var month: Int? = 0
    var year:Int? = 0
    var date: String? = ""
    var etudes : String? = ""
    var langue :String? = ""

    //DEUX USER LIVE***
    private val _user = MutableLiveData<User>()
    var user_live: LiveData<User> = _user

    private val _userProfil = MutableLiveData<User>()
    var user_liveProfil: LiveData<User> = _userProfil

    private val _ami = MutableLiveData<Boolean>()
    var ami_live: LiveData<Boolean> = _ami

    private val _emailUserList = MutableLiveData<List<User>>()
    val emailUserList: LiveData<List<User>> = _emailUserList

    private val _interetList = MutableLiveData<List<String>>()
    val interetList: LiveData<List<String>> = _interetList

    fun fetchUserData() {
        repository.getUserData().observeForever { user ->
            _user.value = user
        }
    }

    fun fetchInteret(){
        repository.fetchInteret().observeForever{ interet ->
            _interetList.value = interet
        }
    }

    fun editProfil(){
        repository.editProfil(user_live.value)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun editProfil(view: View){

        if(!verificatioAge(user_live.value!!.date!!)){
            toastShow(view.context,"Votre date de naissance n'est pas valide")
        }
        else{
            repository.editProfil(user_live.value)
            toastShow(view.context,"Votre profil a bien été modifié")
            goToActivityWithoutArgs(view.context,ManageHomeActivity::class.java)

        }
    }

    fun fetchUserProfilData(profilUid: String?) {
        repository.getUserProfilData(profilUid).observeForever { user ->
            //Alex pour n'avoir qu'une seul live data
            _userProfil.value = user
        }
    }

    fun verifAmitier(profilUid: String?){
        repository.verifAmitier(profilUid).observeForever { ami ->
            _ami.value = ami
        }
    }

    fun addOrDelete(){
        if(ami_live.value == true){
            repository.removeFriend(profilUid)
        }
        else if (ami_live.value == false){
            repository.addFriendRequestToUserByUid(profilUid)
        }
        verifAmitier(profilUid)
    }

    fun signaler(view: View){
        goToActivityWithArgs(view.context,SignalementActivity::class.java,"uid" to profilUid.toString())

    }

    fun sendSignalement(){
        if (!messSignalement.equals("")){
            repository.sendSignalement(profilUid, messSignalement)
        }
    }

    @SuppressLint("RestrictedApi")
    fun logout(view: View){
        repository.logout()
        val activity = view.context as Activity

        activity.finishAffinity()
        activity.finish()


    }

    fun fetchUsersFriends() {
        repository.fetchUsersFriends().observeForever{ user ->
            _emailUserList.value = user
        }
    }

    fun goToAddContact(view: View){
        goToActivityWithoutArgs(view.context,AddContactActivity::class.java)

    }

    fun onSelectItem(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        langue = parent!!.adapter.getItem(pos).toString()
        if (user_live.value != null){
            user_live.value!!.langue = langue
        }
    }

    fun onSelectItem2(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        etudes = parent!!.adapter.getItem(pos).toString()
        if (user_live.value != null) {
            user_live.value!!.etude = etudes
        }
    }

    fun changeDate(year: Int, month: Int, day: Int) {
        val dayString =  if (day < 10) "0$day" else day.toString()
        val monthString = if (month <= 10) "0${month + 1}" else "${month + 1}"
        date = "$dayString/$monthString/$year"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun verificatioAge(date: String): Boolean {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val dateNaissance = LocalDate.parse(date, formatter)
        val age = ChronoUnit.YEARS.between(dateNaissance, LocalDate.now())
        val estMajeur = age >= 18 && dateNaissance.isBefore(LocalDate.now())
        return estMajeur
    }

    fun registerPhoto(photo: Uri, context: Context, path : String){
        if(path == "photos/"){
            user_live.value?.img = repository.registerPhoto(photo, context, path)

        }
        if(path == "photosCover/") {
            user_live.value?.imgCover = repository.registerPhoto(photo, context,path)
        }
    }

}