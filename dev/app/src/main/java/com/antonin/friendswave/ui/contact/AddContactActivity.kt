package com.antonin.friendswave.ui.contact

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.antonin.friendswave.outils.toastShow
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.antonin.friendswave.R
import com.antonin.friendswave.adapter.ListGeneriqueAdapter
import com.antonin.friendswave.data.model.User
import com.antonin.friendswave.databinding.ActivityAddContactBinding
import com.antonin.friendswave.outils.goToActivityWithArgs
import com.antonin.friendswave.strategy.*
import com.antonin.friendswave.ui.authentification.InterfaceAuth
import com.antonin.friendswave.ui.home.ProfilActivity
import com.antonin.friendswave.ui.viewModel.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Activité qui permet d'ajouter un contact'

class AddContactActivity : AppCompatActivity(), KodeinAware, InterfaceAuth {

    override val kodein : Kodein by kodein()
    private lateinit var viewModel: ContactViewModel
    private val factory : ContactViewModelFactory by instance()
    private lateinit var adapter1 : ListGeneriqueAdapter<User>
    private lateinit var binding: ActivityAddContactBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_contact)
        viewModel = ViewModelProviders.of(this, factory)[ContactViewModel::class.java]

        binding.viewmodel = viewModel
        viewModel.interfaceAuth = this
        binding.lifecycleOwner = this
        binding.root
    }

    override fun onResume() {
        super.onResume()
        //pour verifier si la personne est dans l'app
        viewModel.fetchEmail()
        //pour les strategies de referencement
        viewModel.fetchAllUser()
        viewModel.fetchUserData()
        binding.noResultFound.visibility = View.GONE
        adapter1 = ListGeneriqueAdapter(R.layout.recycler_contact)
        val layoutManager2 = LinearLayoutManager(this)
        binding.recyclerSuggestion.layoutManager = layoutManager2
        binding.recyclerSuggestion.adapter = adapter1

        val searchHobbyFriend = SearchHobbyFriend()
        val searchCityFriend = SearchCityFriend()
        val searchAgeFriend = SearchAgeFriend()
        var searchStrategyFriend: StrategyFriend

        binding.btnHobby.setOnClickListener{
            searchStrategyFriend = StrategyFriend(searchHobbyFriend)
            strategyUser(searchStrategyFriend)
        }
        binding.btnCity.setOnClickListener{
            searchStrategyFriend = StrategyFriend(searchCityFriend)
            strategyUser(searchStrategyFriend)
        }
        binding.btnAge.setOnClickListener{
            searchStrategyFriend = StrategyFriend(searchAgeFriend)
            strategyUser(searchStrategyFriend)
        }
    }

    fun strategyUser(strategy: StrategyFriend) {

        viewModel.totalUserList.observe(this) { userList ->
            val tempList = strategy.search(viewModel.user_live.value, userList) as ArrayList<User>
            adapter1.addItems(tempList)

            if (tempList.isEmpty()) {
                binding.noResultFound.visibility = View.VISIBLE

            } else {
                binding.noResultFound.visibility = View.GONE
            }

            adapter1.setOnListItemViewClickListener(object :
                ListGeneriqueAdapter.OnListItemViewClickListener {
                override fun onClick(view: View, position: Int) {
                    val userChoisi = tempList.get(position)

                    goToActivityWithArgs(view.context,ProfilActivity::class.java,"uid" to userChoisi.uid.toString())
                }
            })
        }
    }

    override fun onSuccess() {
        TODO("Not yet implemented")
    }

    override fun onFailure(message: String) {
        toastShow(this,message)

    }


}