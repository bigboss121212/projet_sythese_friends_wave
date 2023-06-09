package com.antonin.friendswave.ui.home

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.antonin.friendswave.R
import com.antonin.friendswave.adapter.MyGridViewAdapter
import com.antonin.friendswave.data.firebase.FirebaseSourceUser
import com.antonin.friendswave.data.repository.UserRepo
import com.antonin.friendswave.databinding.ActivityEditProfilBinding
import com.antonin.friendswave.ui.viewModel.HomeFragmentVMFactory
import com.antonin.friendswave.ui.viewModel.HomeFragmentViewModel
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Activité qui permet d'éditer son profil'
// les méthodes pour la position sont les memes que pour AddEventActivity
// pour le gridview et la classe MyGridViewAdapter: https://www.geeksforgeeks.org/gridview-using-baseadapter-in-android-with-example/
//spinner : https://developer.android.com/develop/ui/views/components/spinner

class EditProfilActivity : AppCompatActivity(), KodeinAware {

    override val kodein : Kodein by kodein()
    private val factory : HomeFragmentVMFactory by instance()
    private var viewModel: HomeFragmentViewModel = HomeFragmentViewModel(
        repository = UserRepo(firebaseUser = FirebaseSourceUser()))
    var addressList: List<Address>? = null
    lateinit var address : Address
    private lateinit var binding: ActivityEditProfilBinding
    private lateinit var adapter: MyGridViewAdapter
    private val AUTOCOMPLETE_REQUEST_CODE = 1

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profil)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profil)
        viewModel = ViewModelProviders.of(this, factory)[HomeFragmentViewModel::class.java]
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.api_key_google_map))
        }

        viewModel.fetchUserData()
        viewModel.fetchInteret()
        actualiserSpinner()
        afficheInteret()

        viewModel.user_live.observe(this) {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val date = LocalDate.parse(it.date, formatter)
            viewModel.day = date.dayOfMonth
            viewModel.month = date.monthValue
            viewModel.year = date.year
        }

        binding.pictureProfil.setOnClickListener {
            val img = Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            getResult.launch(img)
        }

        binding.pictureCover.setOnClickListener{
            val img = Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            getResultCover.launch(img)
        }

        binding.txtLieuEdit.setOnClickListener {
            val fields = listOf(Place.Field.ID, Place.Field.ADDRESS)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }
    }

    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            if (it.resultCode == Activity.RESULT_OK) {
                val img_uri = it?.data?.data!!
                binding.imgProfil.setImageURI(img_uri)
                val path = "photos/"
                viewModel.registerPhoto(img_uri, this, path)
            }
        }


    private val getResultCover =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            if (it.resultCode == Activity.RESULT_OK) {
                val img_uri = it?.data?.data!!
                binding.imageCover.setImageURI(img_uri)
                val path = "photosCover/"
                viewModel.registerPhoto(img_uri, this, path)
            }
        }

    fun actualiserSpinner(){
        var positionToSelect = 0
        val mySpinnerL = binding.spinnerLangues
        val mySpinnerE = binding.spinnerEtudes
        //pour inserer les donnees actuelles de l'user dans les spinners
        binding.viewmodel!!.user_live.observe(this){
            for (i in 0 until mySpinnerL.count) {
                if (mySpinnerL.getItemAtPosition(i).toString() == it?.langue) {
                    positionToSelect = i
                    break
                }
            }
            mySpinnerL.setSelection(positionToSelect)
            positionToSelect = 0
            for (i in 0 until mySpinnerE.count) {
                if (mySpinnerE.getItemAtPosition(i).toString() == it?.etude) {
                    positionToSelect = i
                    break
                }
            }
            mySpinnerE.setSelection(positionToSelect)
        }
    }

    fun afficheInteret(){
        binding.viewmodel!!.interetList.observe(this){
            adapter = MyGridViewAdapter(this, it, viewModel)
            binding.gridView.adapter = adapter
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
                        binding.editLieu.setText(place.address!!.toString().split(",")[0])

                        val geoCoder = Geocoder(this)
                        try {
                            addressList = geoCoder.getFromLocationName(place.address!!.toString(), 1)

                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        address = addressList!![0]
                    }

                    viewModel.user_live.value?.longitude = address.longitude
                    viewModel.user_live.value?.lattitude = address.latitude
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





