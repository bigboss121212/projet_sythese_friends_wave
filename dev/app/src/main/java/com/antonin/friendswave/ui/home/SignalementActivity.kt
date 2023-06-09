package com.antonin.friendswave.ui.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.antonin.friendswave.R
import com.antonin.friendswave.data.firebase.FirebaseSourceUser
import com.antonin.friendswave.data.repository.UserRepo
import com.antonin.friendswave.databinding.ActivitySignalementBinding
import com.antonin.friendswave.ui.viewModel.HomeFragmentVMFactory
import com.antonin.friendswave.ui.viewModel.HomeFragmentViewModel
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Activité qui permet de signaler un utilisateur

class SignalementActivity : AppCompatActivity(), KodeinAware {

    override val kodein : Kodein by kodein()
    private val factory : HomeFragmentVMFactory by instance()
    private var viewModel: HomeFragmentViewModel = HomeFragmentViewModel(repository = UserRepo(firebaseUser = FirebaseSourceUser()))
    private lateinit var binding: ActivitySignalementBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signalement)

        val profilUid = intent.getStringExtra("uid")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_signalement)
        viewModel = ViewModelProviders.of(this, factory)[HomeFragmentViewModel::class.java]
//        val view = binding.root
        binding.view = viewModel
        binding.lifecycleOwner = this
        binding.view?.profilUid = profilUid
    }
}