package com.antonin.friendswave.ui.event

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.antonin.friendswave.R
import com.antonin.friendswave.adapter.ListGeneriqueAdapter
import com.antonin.friendswave.data.model.User
import com.antonin.friendswave.databinding.ActivityRatingBinding
import com.antonin.friendswave.ui.viewModel.EventFragmentVMFactory
import com.antonin.friendswave.ui.viewModel.EventFragmentViewModel
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Activité qui permet de noter un event

// Activité qu'on a pas pu finir, on aurait aimé la relier a une trigger function pour qu'a chaque fin d'event les utilisateurs puissent recevoir une notif et qu'il soit rediriger vers cette page

class RatingActivity : AppCompatActivity(), KodeinAware {

    override val kodein : Kodein by kodein()
    private lateinit var viewModel: EventFragmentViewModel
    private val factory : EventFragmentVMFactory by instance()
    private lateinit var adapter1 : ListGeneriqueAdapter<User>
    private lateinit var binding: ActivityRatingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_rating)
        viewModel = ViewModelProviders.of(this, factory)[EventFragmentViewModel::class.java]
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
    }

}