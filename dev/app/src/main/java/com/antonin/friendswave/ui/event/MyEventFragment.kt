package com.antonin.friendswave.ui.event

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.antonin.friendswave.R
import com.antonin.friendswave.adapter.ListGeneriqueAdapter
import com.antonin.friendswave.data.firebase.FirebaseSourceEvent
import com.antonin.friendswave.data.firebase.FirebaseSourceUser
import com.antonin.friendswave.data.model.Event
import com.antonin.friendswave.data.repository.EventRepo
import com.antonin.friendswave.data.repository.UserRepo
import com.antonin.friendswave.databinding.FragmentMyEventBinding
import com.antonin.friendswave.outils.goToActivityWithArgs
import com.antonin.friendswave.ui.viewModel.EventFragmentVMFactory
import com.antonin.friendswave.ui.viewModel.EventFragmentViewModel
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Activité qui permet de voir ses events

class MyEventFragment : Fragment(), KodeinAware {

    override val kodein : Kodein by kodein()
    private val factory : EventFragmentVMFactory by instance()
    private lateinit var binding : FragmentMyEventBinding
    private var viewModel: EventFragmentViewModel = EventFragmentViewModel(repository = UserRepo(firebaseUser = FirebaseSourceUser()),
        repoEvent = EventRepo(firebaseEvent = FirebaseSourceEvent()))
    private var adapter1 : ListGeneriqueAdapter<Event> = ListGeneriqueAdapter(R.layout.recycler_events)
    private var adapter2 : ListGeneriqueAdapter<Event> = ListGeneriqueAdapter(R.layout.recycler_events)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this,factory)[EventFragmentViewModel::class.java]
        viewModel.fetchEventsPrivateUser()
        viewModel.fetchEventsPublicUser()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_my_event, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        val layoutManager = LinearLayoutManager(context)
        val layoutManager1 = LinearLayoutManager(context)

        binding.recyclerMyEventPrivate.layoutManager = layoutManager
        binding.recyclerMyEventPrivate.adapter = adapter1

        binding.recyclerMyEventPublic.layoutManager = layoutManager1
        binding.recyclerMyEventPublic.adapter = adapter2

        viewModel.eventListPrivateUser.observe(this){ eventList ->
            adapter1.addItems(eventList)
            if(adapter1.itemCount != 0){
                binding.createYourFirstEvent.visibility = View.GONE
            }
        }

        viewModel.eventListPublicUser.observe(this){ eventList ->
            adapter2.addItems(eventList)
            if(adapter2.itemCount != 0){
                binding.createYourFirstEventPublic.visibility = View.GONE
            }
        }

        adapter1.setOnListItemViewClickListener(object : ListGeneriqueAdapter.OnListItemViewClickListener{
            override fun onClick(view: View, position: Int) {
               val eventKey = viewModel.eventListPrivateUser.value!!.get(position).key.toString()
                goToActivityWithArgs(view.context,MyEventManageActivity::class.java,"clef" to eventKey, "pos" to position )

            }
        })

        adapter2.setOnListItemViewClickListener(object:ListGeneriqueAdapter.OnListItemViewClickListener {
            override fun onClick(view: View, position: Int) {
                val eventKey = viewModel.eventListPublicUser.value!!.get(position).key.toString()
                goToActivityWithArgs(view.context,MyEventManageActivity::class.java,"clef" to eventKey, "pos" to position )

            }
        })
    }
}