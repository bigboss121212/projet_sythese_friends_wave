package com.antonin.friendswave.ui.viewModel

import android.content.DialogInterface
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.AdapterView
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.antonin.friendswave.data.model.Event
import com.antonin.friendswave.data.model.User
import com.antonin.friendswave.data.repository.EventRepo
import com.antonin.friendswave.data.repository.UserRepo
import com.antonin.friendswave.outils.*
import com.antonin.friendswave.ui.chat.GroupChatActivity
import com.antonin.friendswave.ui.event.*
import com.antonin.friendswave.ui.home.ManageHomeActivity
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Classe permettant de gerer le ViewModel pour les events'

class EventFragmentViewModel(private val repository:UserRepo,private val repoEvent:EventRepo):ViewModel() {

    var name: String? = ""
    var description: String? = ""
    var photo: Uri? = null
    var nbrePersonnes : Int? = 0
    var isPublic :Boolean = false
    var categorie: String? = ""
    var adress: String? = ""
    var lattitude: String? = ""
    var longitude : String?  = ""
    var date: String? = ""
    var horaire: String? = ""
    var day: Int? = 0
    var month: Int? = 0
    var year:Int? = 0
    var hour:Int? = 0
    var minute: Int? = 0
    var duree: Int? = 0
    var timeStamp : Double = 0.0
    var email: String? = ""
    var keyEvent: String? = ""
    val isPublicChecked: MutableLiveData<Boolean> = MutableLiveData()
    var strCategory = MutableLiveData<String>()

    val user by lazy {
        repository.currentUser()
    }

    private val _user = MutableLiveData<User>()
    var user_live: LiveData<User> = _user

    private val _admin = MutableLiveData<User>()
    var admin_live: LiveData<User> = _admin

    private val _guestList = MutableLiveData<List<User>>()
    val guestList: LiveData<List<User>> = _guestList

    private val _guestListPublic = MutableLiveData<List<User>>()
    val guestListPublic: LiveData<List<User>> = _guestListPublic

    private val _pending_guest_list = MutableLiveData<List<User>>()
    val pending_guest_list: LiveData<List<User>> = _pending_guest_list

    private val _confirm_guestListPublic = MutableLiveData<List<User>>()
    val confirm_guestListPublic: LiveData<List<User>> = _confirm_guestListPublic

    private val _eventData = MutableLiveData<Event>()
    val eventData: LiveData<Event> = _eventData

    private val _eventDataPublic = MutableLiveData<Event>()
    val eventDataPublic: LiveData<Event> = _eventDataPublic

    private val _eventPendingPublic = MutableLiveData<List<Event>>()
    val eventPendingPublic: LiveData<List<Event>> = _eventPendingPublic

    private val _eventList = MutableLiveData<List<Event>>()
    val eventList: LiveData<List<Event>> = _eventList

    private val _eventListPrivateUser = MutableLiveData<List<Event>>()
    val eventListPrivateUser: LiveData<List<Event>> = _eventListPrivateUser

    private val _eventListPublicUser = MutableLiveData<List<Event>>()
    val eventListPublicUser : LiveData<List<Event>> = _eventListPublicUser

    private val _eventListConfirm = MutableLiveData<List<Event>>()
    val eventListConfirm: LiveData<List<Event>> = _eventListConfirm


    private val _emailList = MutableLiveData<List<String>>()
    val emailList: LiveData<List<String>> = _emailList

    fun fetchEmail() {
        repository.fetchfetchEmail().observeForever{ email ->
            _emailList.value = email
        }
    }

    fun fetchUserData() {
        repository.getUserData().observeForever { user ->
            _user.value = user
        }
    }

    fun fetchDataEvent(key: String) {
        repoEvent.getEventData(key).observeForever { event ->
            _eventData.value = event
        }
    }

    fun goToAddEvent(view: View){
        goToActivityWithoutArgs(view.context,AddEventActivity::class.java)
    }

    fun sendRequestToParticipatePublicEvent(idEvent:String, adminEvent:String, view: View){

        if(user_live.value!!.eventConfirmationList!!.containsKey(idEvent))
            toastShow(view.context,"Vous participez dejà à cet évenement")

        else if(user_live.value!!.pendingRequestEventPublic!!.containsKey(idEvent))
            toastShow(view.context,"Demande dejà envoyée")

        // Si user est different de l'admin de l'event, il peut faire une demande :
        else if(adminEvent != user!!.uid){
            repoEvent.sendRequestToParticipatePublicEvent(idEvent,adminEvent)
            toastShow(view.context,"Demande envoyée")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addEventUser(view: View) {

        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val dateFormat = LocalDate.parse(date, formatter)
        isPublicChecked.value = isPublic

        if (name?.isNotEmpty() == true
            && nbrePersonnes != 0
            && user?.uid?.isNotEmpty() == true
            && categorie?.isNotEmpty() == true
            && date?.isNotEmpty() == true
            && horaire?.isNotEmpty() == true
            && adress?.isNotEmpty() == true
            && description?.isNotEmpty() == true
            && duree != 0)
        {
            if(photo == null)
                toastShow(view.context,"Veuillez insérer une photo")
            else if(nbrePersonnes!! > 10)
                toastShow(view.context,"Il ne peut pas y avoir plus de 10 personnes a un event")
            else if(dateFormat.isBefore(LocalDate.now()))
                toastShow(view.context,"La date doit etre supérieure a celle d'aujoud'hui")
            else{
                repoEvent.addEventUser(name!!, isPublic,nbrePersonnes!!,
                    user!!.uid, categorie!!,
                    date!!, horaire!!,
                    adress!!,description!!,
                    longitude!!,lattitude!!,
                    photo!!,view.context, user!!.displayName.toString(),
                    timeStamp, duree!!)
                }
            toastShow(view.context,"Evenement en cours de publication")
            goToActivityWithoutArgs(view.context,ManageHomeActivity::class.java)

        }
         else toastShow(view.context,"Veuillez remplir tous les champs")
    }
    // Vérifie la condition du switch, soit sur true (event public) ou false (event private)
    fun executeOnStatusChanged(isChecked: Boolean) {
        isPublic = isChecked
        isPublicChecked.value = isChecked
    }


    // pour recuperer la valeur de la categorie dans le spinner :
    fun onSelectItem(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
       categorie = parent!!.adapter.getItem(pos).toString()
    }

    // ViewModel pour MyEvent :
    fun changeDate(view: View,year: Int, month: Int, day: Int) {

        val dayString =  if (day < 10) "0$day" else day.toString()
        val monthString = if (month <= 10) "0${month + 1}" else "${month + 1}"
        date = dayString + "/"+monthString +"/"+ year.toString()

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        var strDate: Date? = sdf.parse(date.toString())

        if (strDate != null)  timeStamp = strDate.time.toDouble()
        if (Date().after(strDate))  toastShow(view.context,"Impossible de remonter dans le temps")

    }

    fun changeHour(hour:Int, minute:Int) {
        val hourString = if (hour < 10) "0$hour" else hour.toString()
        val minuteString = if (minute < 10) "0$minute" else minute.toString()
        horaire = hourString + ":" + minuteString
    }

    fun fetchGuestConfirmDetailEventPublic(key: String?){
        repoEvent.fetchGuestConfirmDetailEventPublic(key).observeForever{ user ->
            _confirm_guestListPublic.value = user
        }
    }

    fun fetchAdmin(uid: String){
        repository.fetchAdmin(uid).observeForever { user ->
            _admin.value = user
        }
    }

    fun getAllEventsPendingRequestPublic(){
        repoEvent.getAllEventsPendingRequestPublic().observeForever{ event ->
            _eventPendingPublic.value= event
        }
    }

    fun fetchGuestDetailEventPublic(key:String?){
        repoEvent.fetchGuestDetailEventPublic(key).observeForever{ event->
            _guestListPublic.value = event
        }
    }

    fun fetchPendingGuestEventPublic(key:String?){
        repoEvent.fetchPendingGuestEventPublic(key).observeForever{ event->
            _pending_guest_list.value = event
        }
    }

    fun fetchEvents() {
        repoEvent.fetchEvents().observeForever{ event ->
        _eventList.value = event
        }
    }

    fun fetchEventsPrivateUser() {
        repoEvent.fetchEventsPrivateUser().observeForever{ event ->
            _eventListPrivateUser.value = event
        }
    }

    fun fetchEventsPublicUser() {
        repoEvent.fetchEventsPublicUser().observeForever{ event ->
            _eventListPublicUser.value = event
        }
    }

    fun fetchConfirmationEvents() {
        repoEvent.fetchConfirmationEvents().observeForever{event ->
            _eventListConfirm.value = event
        }
    }

    fun fetchDetailEventPublicUser(key:String?) {
        repoEvent.fetchDetailEventPublicUser(key).observeForever{ event->
            _eventDataPublic.value = event
        }
    }

    fun sendAnInvitationEvent(view:View, key: String){
        val alertDialog = AlertDialog()


        val positiveButtonClickListener = DialogInterface.OnClickListener { dialog, which ->
            // Code à exécuter si le bouton positif est cliqué
            if (which == DialogInterface.BUTTON_POSITIVE) {
                sendEmail(email!!, user_live.value!!.email!!,user_live.value!!.name!!)
                toastShow(view.context,"demande envoyée par courriel")
                alertDialog.cancel()
            }
        }
        val negativeButtonClickListener= DialogInterface.OnClickListener { dialog, which ->
            // Code à exécuter si le bouton négatif est cliqué
            if (which == DialogInterface.BUTTON_NEGATIVE) {
                toastShow(view.context,"ok on ne touche à rien")
                alertDialog.cancel()
            }
        }

        if (email.isNullOrEmpty() || !email!!.matches(emailRegex)) {
            toastShow(view.context,"Courriel vide ou non valide")
            return
        }
        else if(!emailList.value!!.contains(email)) {
            alertDialog.showDialog(view.context,
                "Attention",
                "This email match no account, do you want to make an invitation via email",
                "yes",
                "no",
                positiveButtonClickListener,
                negativeButtonClickListener)

        }
        if(eventData.value?.key == key){
            repoEvent.sendAnInvitationEvent(email!!, eventData.value!!)
        }
        if(eventDataPublic.value?.key == key){
            repoEvent.sendAnInvitationEvent(email!!, eventDataPublic.value!!)
        }
        toastShow(view.context,"Invitation envoyée")
        email = ""
    }

    fun gotoMesEventsActivity(view: View) {
        goToActivityWithoutArgs(view.context,ManagerFragmentEvent::class.java)
    }

    fun editEvent(){
        if(_eventDataPublic.value!!.date!!.matches(patternDate)){
            repoEvent.editEvent(_eventDataPublic.value)
        }
    }

    fun  deleteEvent() {
        repoEvent.deleteEvent(_eventDataPublic.value)
    }

    fun deleteConfirmation(event:Event?) {

        _eventData.value!!.nbreInscrit?.minus(1)
        repoEvent.deleteConfirmation(event)
    }

    fun deleteConfirmationGuest(event:Event?,idGuest:String){

        repoEvent.deleteConfirmationGuest(event,idGuest)
    }

    fun deletePendingEvent(event: Event?){

        repoEvent.deletePendingEvent(event)
    }

    fun startGroupChat(view: View, eventKey: String, admin: String){
        goToActivityWithArgs(view.context,GroupChatActivity::class.java,"eventKey" to eventKey, "admin" to admin)

    }
}



