package com.antonin.friendswave.ui.home

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.antonin.friendswave.R
import com.antonin.friendswave.data.firebase.FirebaseSourceUser
import com.antonin.friendswave.data.firebase.FirebaseStore
import com.antonin.friendswave.data.repository.UserRepo
import com.antonin.friendswave.databinding.ActivityProfilBinding
import com.antonin.friendswave.outils.AnimationLayout
import com.antonin.friendswave.ui.viewModel.HomeFragmentVMFactory
import com.antonin.friendswave.ui.viewModel.HomeFragmentViewModel
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Activité qui permet de voir le profil des autres utilisateurs

class ProfilActivity : AppCompatActivity(), KodeinAware {

    private val storeMedia = FirebaseStore()
    override val kodein : Kodein by kodein()
    private val factory : HomeFragmentVMFactory by instance()
    private var viewModel: HomeFragmentViewModel = HomeFragmentViewModel(
        repository = UserRepo(firebaseUser = FirebaseSourceUser()))
    private lateinit var binding: ActivityProfilBinding
    private var bool = true
    private var anim = AnimationLayout()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)

        val profilUid = intent.getStringExtra("uid")

        binding= DataBindingUtil.setContentView(this, R.layout.activity_profil)
        viewModel = ViewModelProviders.of(this, factory)[HomeFragmentViewModel::class.java]
        binding.item = viewModel
        binding.lifecycleOwner = this
        binding.item?.profilUid = profilUid

        if(profilUid != null){
            viewModel.fetchUserProfilData(profilUid)
            binding.item?.verifAmitier(profilUid)
        }

        viewModel.fetchUserData()

        viewModel.ami_live.observe(this) {
            if (viewModel.ami_live.value == true) {
                binding.floatingActionButton.setText(R.string.recycler_delete)
            }
            if (viewModel.ami_live.value == false) {
                binding.floatingActionButton.setText(R.string.add)
                binding.floatingActionButton.setBackgroundColor(Color.CYAN)
            }
        }

        viewModel.user_liveProfil.observe(this) {
            val path1 = "photos/" + it.img.toString()
            val path2 = "photosCover/" + it.imgCover.toString()

            storeMedia.displayImage(binding.imgProfil, path1)
            storeMedia.displayImage(binding.imageCover, path2)
        }

        binding.linearDescriptionProfil.setOnClickListener{

            if(bool){

                anim.expand(binding.linearDescriptionProfil, 1000,500)
                bool = false
            } else {

                anim.collapse(binding.linearDescriptionProfil,1000, 40)
                bool = true
            }
        }
    }
}