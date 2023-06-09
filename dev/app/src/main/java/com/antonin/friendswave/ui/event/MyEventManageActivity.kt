package com.antonin.friendswave.ui.event
import android.app.Activity
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.antonin.friendswave.outils.toastShow
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.antonin.friendswave.R
import com.antonin.friendswave.adapter.ListGeneriqueAdapter
import com.antonin.friendswave.data.firebase.FirebaseSourceEvent
import com.antonin.friendswave.data.firebase.FirebaseSourceUser
import com.antonin.friendswave.data.firebase.FirebaseStore
import com.antonin.friendswave.data.model.Event
import com.antonin.friendswave.data.model.User
import com.antonin.friendswave.data.repository.EventRepo
import com.antonin.friendswave.data.repository.UserRepo
import com.antonin.friendswave.databinding.ActivityMyEventManageBinding
import com.antonin.friendswave.outils.AlertDialog
import com.antonin.friendswave.outils.AnimationLayout
import com.antonin.friendswave.outils.goToActivityWithArgs
import com.antonin.friendswave.ui.home.ProfilActivity
import com.antonin.friendswave.ui.viewModel.EventFragmentVMFactory
import com.antonin.friendswave.ui.viewModel.EventFragmentViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.io.IOException

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Activité qui permet d"éditer son event"

class MyEventManageActivity : AppCompatActivity(), KodeinAware {

    override val kodein: Kodein by kodein()
    private val factory: EventFragmentVMFactory by instance()
    val storeMedia = FirebaseStore()
    private var viewModel: EventFragmentViewModel = EventFragmentViewModel(
        repository = UserRepo(firebaseUser = FirebaseSourceUser()), repoEvent = EventRepo(firebaseEvent = FirebaseSourceEvent()))
    private var adapter1: ListGeneriqueAdapter<User> = ListGeneriqueAdapter(R.layout.recycler_inscrit_event)
    private var adapter2: ListGeneriqueAdapter<User> = ListGeneriqueAdapter(R.layout.recycler_contact)
    private val AUTOCOMPLETE_REQUEST_CODE = 1
    private val ecouteur = Ecouteur()
    private lateinit var binding: ActivityMyEventManageBinding
    var addressList: List<Address>? = null
    private lateinit var address: Address

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_event_manage)

        val key_event = intent.getStringExtra("clef")
        val binding: ActivityMyEventManageBinding = DataBindingUtil.setContentView(this, R.layout.activity_my_event_manage)
        viewModel = ViewModelProviders.of(this, factory)[EventFragmentViewModel::class.java]
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        viewModel.fetchEmail()

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.api_key_google_map))
        }

        if (key_event != null) {
            binding.clef = key_event
            viewModel.fetchDetailEventPublicUser(key_event) // event public
            viewModel.fetchGuestDetailEventPublic(key_event) // chercher les invitations dans l'event Public
            viewModel.fetchGuestConfirmDetailEventPublic(key_event)
            viewModel.fetchPendingGuestEventPublic(key_event)
            viewModel.keyEvent = key_event

            viewModel.eventDataPublic.observe(this) {
                val path1 = "photosEvent/" + it.imgEvent.toString()
                storeMedia.displayImage(binding.imagePreviewEvent, path1 )
            }
        }

        val layoutManager = LinearLayoutManager(this)
        val layoutManager1 = LinearLayoutManager(this)

        binding.recyclerEventInscrit.layoutManager = layoutManager
        binding.recyclerEventInscrit.adapter = adapter1

        binding.recyclerPendingParticipants.layoutManager = layoutManager1
        binding.recyclerPendingParticipants.adapter = adapter2

        viewModel.guestList.observe(this){ guestList ->
            adapter1.addItems(guestList)
        }

        viewModel.confirm_guestListPublic.observe(this) { confirm_guestList ->
            adapter1.addItems(confirm_guestList)
        }
        viewModel.pending_guest_list.observe(this){ pending_guest_list ->
            adapter2.addItems(pending_guest_list)
        }

        binding.btnDeleteMyEvent.setOnClickListener {
            val alert = AlertDialog()
            alert.showDialog(this, "title", "message", "OK",
                "Cancel", positiveButtonClickListener, negativeButtonClickListener)
        }

        binding.editCity.setOnClickListener {
            val fields = listOf(Place.Field.ID, Place.Field.ADDRESS)
            val intent =
                Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }

        viewModel.guestListPublic.observe(this){ attente_guestList ->
            adapter2.addItems(attente_guestList)
        }

        binding.linearInvitation.setOnClickListener(ecouteur)
        binding.linearDescription.setOnClickListener(ecouteur)
        binding.linearInscrit.setOnClickListener(ecouteur)
        binding.linearAttenteExpand.setOnClickListener(ecouteur)

        adapter1.setOnListItemViewClickListener(object : ListGeneriqueAdapter.OnListItemViewClickListener {
            override fun onClick(view: View, position: Int) {
                val event = viewModel.eventDataPublic.value!!
                val idGuest = viewModel.confirm_guestListPublic.value!!.get(position).uid

                if(view.id == R.id.btn_delete_guest){
                    val alert = AlertDialog()
                    alert.showDialog(this@MyEventManageActivity,
                        "Etes vous sur de supprimer ce participant",
                        "Attention, vous êtes sur le point de supprimer ce participant a cet event",
                        " je confirme",
                        "Annuler", positiveDeleteGuest(event,idGuest!!), negativeButtonClickListener)

                }else {
                    goToActivityWithArgs(view.context,ProfilActivity::class.java,"uid" to idGuest!!)
                }
            }
        })

        adapter2.setOnListItemViewClickListener(object:ListGeneriqueAdapter.OnListItemViewClickListener{
            override fun onClick(view: View, position: Int) {
                val idGuest = viewModel.pending_guest_list.value!!.get(position).uid
                goToActivityWithArgs(view.context,ProfilActivity::class.java,"uid" to idGuest!!)
            }
        })

    }

    fun positiveDeleteGuest(event: Event?, idGuest:String) = DialogInterface.OnClickListener { _, which ->
        if(which == DialogInterface.BUTTON_POSITIVE) {
            viewModel.deleteConfirmationGuest(event!!,idGuest)
            finish()
        }
    }

    val positiveButtonClickListener = DialogInterface.OnClickListener { _, which ->
        // Code à exécuter si le bouton positif est cliqué
        if (which == DialogInterface.BUTTON_POSITIVE) {
            viewModel.deleteEvent()
            finish()
        }
    }

    val negativeButtonClickListener = DialogInterface.OnClickListener { _, which ->
        // Code à exécuter si le bouton négatif est cliqué
        if (which == DialogInterface.BUTTON_NEGATIVE) {
            toastShow(this,"ok on ne touche à rien")
        }
    }

    // methode de Google dans la doc:
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        binding.adress.text = place.address?.toString()
                        val geoCoder = Geocoder(this)
                        try {
                            addressList =
                                geoCoder.getFromLocationName(place.address!!.toString(), 1)

                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        address = addressList!![0]
                    }
                    viewModel.longitude = address.longitude.toString()
                    viewModel.lattitude = address.latitude.toString()
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // TODO: Handle the error.
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Log.i(ContentValues.TAG, status.statusMessage ?: "")
                    }
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
    private class Ecouteur : View.OnClickListener {

        private var bool_linear_attente: Boolean = true
        private var bool_linear_description: Boolean = true
        private var bool_linear_invitation: Boolean = true
        private var bool_linear_inscrit: Boolean = true
        private val animation = AnimationLayout()

        override fun onClick(view: View?) {

            if(view!!.id == R.id.linearAttenteExpand){
                if (bool_linear_attente) {
                    bool_linear_attente = false
                    animation.expand(view.rootView.findViewById(R.id.linearAttenteExpand), 1000, 800)
                } else {
                    animation.collapse(view.rootView.findViewById(R.id.linearAttenteExpand), 1000, 40)
                    bool_linear_attente = true
                }
            }
            if(view.id == R.id.linearInscrit){
                if (bool_linear_inscrit) {
                    bool_linear_inscrit = false

                    animation.expand(view.rootView.findViewById(R.id.linearInscrit), 1000, 800)
                } else {
                    animation.collapse(view.rootView.findViewById(R.id.linearInscrit), 1000, 40)
                    bool_linear_inscrit = true
                }
            }
            if(view.id == R.id.linear_description){

                if (bool_linear_description) {
                    bool_linear_description = false

                    animation.expand(view.rootView.findViewById(R.id.linear_description), 1000, 800)
                } else {
                    animation.collapse(view.rootView.findViewById(R.id.linear_description), 1000, 40)
                    bool_linear_description = true
                }
            }
            if(view.id == R.id.linear_invitation){

                if (bool_linear_invitation) {
                    bool_linear_invitation= false

                    animation.expand(view.rootView.findViewById(R.id.linear_invitation), 1000, 800)
                } else {
                    animation.collapse(view.rootView.findViewById(R.id.linear_invitation), 1000, 40)
                    bool_linear_invitation = true
                }
            }
        }

    }



