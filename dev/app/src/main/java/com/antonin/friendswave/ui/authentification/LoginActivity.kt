package com.antonin.friendswave.ui.authentification

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.antonin.friendswave.R
import com.antonin.friendswave.databinding.ActivityLoginBinding
import com.antonin.friendswave.outils.goToActivityWithoutArgs
import com.antonin.friendswave.outils.toastShow
import com.antonin.friendswave.ui.home.ManageHomeActivity
import com.antonin.friendswave.ui.viewModel.AuthViewModel
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import com.antonin.friendswave.ui.viewModel.AuthViewModelFactory

//Documentation https://openclassrooms.com/fr/courses/4872916-creez-un-backend-scalable-et-performant-sur-firebase/4982767-creez-votre-premier-systeme-dauthentification  // tuto pour faire le login signup

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Activité qui permet de se logguer

class LoginActivity : AppCompatActivity(), InterfaceAuth, KodeinAware {

    override val kodein by kodein()
    private lateinit var viewModel: AuthViewModel
    private val factory : AuthViewModelFactory by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val binding: ActivityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        viewModel = ViewModelProviders.of(this, factory).get(AuthViewModel::class.java)
        binding.view = viewModel

        viewModel.interfaceAuth = this

        viewModel.toastMessage.observe(this) { message ->
            toastShow(this,message)
        }

    }


    override fun onSuccess() {
        goToActivityWithoutArgs(this,ManageHomeActivity::class.java)
//        startHomeActivity()
    }

    override fun onFailure(message: String) {
        toastShow(this,message)
    }

    // Permet d'accéder directement a l'app sans remettre son login :
    override fun onStart() {
        super.onStart()
        viewModel.user?.let {
            goToActivityWithoutArgs(this,ManageHomeActivity::class.java)
//            startHomeActivity()
        }
    }
}