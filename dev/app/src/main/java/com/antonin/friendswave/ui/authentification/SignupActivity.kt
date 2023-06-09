package com.antonin.friendswave.ui.authentification

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProviders
import androidx.databinding.DataBindingUtil
import com.antonin.friendswave.R
import com.antonin.friendswave.databinding.ActivitySingupBinding
import com.antonin.friendswave.outils.goToActivityWithoutArgs
import com.antonin.friendswave.outils.toastShow
import com.antonin.friendswave.ui.home.ManageHomeActivity
import com.antonin.friendswave.ui.viewModel.AuthViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import com.antonin.friendswave.ui.viewModel.AuthViewModelFactory

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Activité qui permet de s'inscire'

//Documentation https://openclassrooms.com/fr/courses/4872916-creez-un-backend-scalable-et-performant-sur-firebase/4982767-creez-votre-premier-systeme-dauthentification  // tuto pour faire le login signup

class SignupActivity : AppCompatActivity(), InterfaceAuth, KodeinAware {

    //    private lateinit var progressbar : ProgressBar
    override val kodein by kodein()
    private val factory : AuthViewModelFactory by instance()
    private lateinit var viewModel: AuthViewModel
    private val AUTOCOMPLETE_REQUEST_CODE = 1
    private lateinit var binding: ActivitySingupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_singup)

        val apiKey = getString(R.string.api_key_google_map)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }


        binding = DataBindingUtil.setContentView(this, R.layout.activity_singup)
        viewModel = ViewModelProviders.of(this, factory)[AuthViewModel::class.java]
        binding.viewmodel = viewModel

        viewModel.interfaceAuth = this
        viewModel.fetchEmail()
        viewModel.fetchAllPseudo()


        binding.searchCity.setOnClickListener {
            val fields = listOf(Place.Field.ID, Place.Field.NAME)

            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).setTypeFilter(
                TypeFilter.CITIES).build(this)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }

    }


    override fun onSuccess() {
        goToActivityWithoutArgs(this, ManageHomeActivity::class.java)

    }

    override fun onFailure(message: String) {
        toastShow(this,message)
    }

    // methode de Google dans la doc:
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        binding.editLieu.setText(place.name.toString())
                    }
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