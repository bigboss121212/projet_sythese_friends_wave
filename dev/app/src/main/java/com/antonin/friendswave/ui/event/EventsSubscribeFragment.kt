package com.antonin.friendswave.ui.event

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.antonin.friendswave.outils.toastShow
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
import com.antonin.friendswave.databinding.FragmentEventsSubscribeBinding
import com.antonin.friendswave.outils.AlertDialog
import com.antonin.friendswave.outils.goToActivityWithArgs
import com.antonin.friendswave.outils.goToActivityWithoutArgs
import com.antonin.friendswave.ui.chat.GroupChatActivity
import com.antonin.friendswave.ui.viewModel.EventFragmentVMFactory
import com.antonin.friendswave.ui.viewModel.EventFragmentViewModel
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Activité qui permet de voir les events pour lesquels on est inscrit

class EventsSubscribeFragment : Fragment(), KodeinAware{

    override val kodein : Kodein by kodein()
    private val factory : EventFragmentVMFactory by instance()
    private lateinit var binding : FragmentEventsSubscribeBinding
    private var viewModel: EventFragmentViewModel = EventFragmentViewModel(repository = UserRepo(firebaseUser = FirebaseSourceUser()),
        repoEvent = EventRepo(firebaseEvent = FirebaseSourceEvent()))
    private var adapter1 : ListGeneriqueAdapter<Event> = ListGeneriqueAdapter(R.layout.recycler_my_event_inscrits)
    private var adapter2 : ListGeneriqueAdapter<Event> = ListGeneriqueAdapter(R.layout.recycler_my_event_inscrits)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this,factory).get(EventFragmentViewModel::class.java)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_events_subscribe, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        return binding.root

    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchConfirmationEvents()
        viewModel.getAllEventsPendingRequestPublic()

        val layoutManager = LinearLayoutManager(context)
        val layoutManager1 = LinearLayoutManager(context)

        binding.recyclerMyEventInscrits.layoutManager = layoutManager
        binding.recyclerMyEventInscrits.adapter = adapter1

        binding.recyclerPending.layoutManager = layoutManager1
        binding.recyclerPending.adapter = adapter2

        viewModel.eventListConfirm.observe(this){ eventList ->
            adapter1.addItems(eventList)
            if(adapter1.itemCount != 0){
                binding.eventConfirmFragment.visibility = View.GONE
            }
        }

        viewModel.eventPendingPublic.observe(this){ eventList ->
            adapter2.addItems(eventList)
            if(adapter2.itemCount != 0){
                binding.eventPendingFragment.visibility = View.GONE
            }
        }

        adapter1.setOnListItemViewClickListener(object : ListGeneriqueAdapter.OnListItemViewClickListener{
            override fun onClick(view: View, position: Int) {

                val event = viewModel.eventListConfirm.value?.get(position)
                if(view.id == R.id.img_refuse_event){
                    val alert = AlertDialog()
                    alert.showDialog(requireContext(),
                        "Suppression de votre participation",
                        "Etes vous certain de vouloir supprimer cet event ?",
                        "Confirmer",
                        "Annuler", clickOnPositiveButton(event), negativeButtonClickListener)

                }
                else {
                    goToActivityWithArgs(view.context,GroupChatActivity::class.java,
                        "eventKey" to event!!.key.toString(),
                        "admin" to event.admin)

                }
            }
        })

        adapter2.setOnListItemViewClickListener(object : ListGeneriqueAdapter.OnListItemViewClickListener{
            override fun onClick(view: View, position: Int) {
                val event = viewModel.eventPendingPublic.value?.get(position)
               if(view.id == R.id.img_refuse_event) {

                   val alert = AlertDialog()
                   alert.showDialog(requireContext(),
                       "Annuler votre participation",
                       "Etes vous certain de vouloir annuler votre demande de participation ?",
                       "Confirmer",
                       "Annuler", cliclPositiveButtonPendingEvent(event), negativeButtonClickListener)

               } else {
                   val bool = true
                   goToActivityWithArgs(view.context,DetailEventActivity::class.java,
                       "idEvent" to event!!.key.toString(),
                        "inscrit_ou_non" to bool, "adminEvent" to event.admin)

                }
            }
        })
    }


    fun clickOnPositiveButton(event:Event?) = DialogInterface.OnClickListener { _, which ->
            if (which == DialogInterface.BUTTON_POSITIVE) {
                viewModel.deleteConfirmation(event)
                goToActivityWithoutArgs(requireContext(), ManagerFragmentEvent::class.java)
            }
        }

    fun cliclPositiveButtonPendingEvent(event:Event?) = DialogInterface.OnClickListener { _, which ->
        if(which == DialogInterface.BUTTON_POSITIVE) {
            viewModel.deletePendingEvent(event)
            goToActivityWithoutArgs(requireContext(), ManagerFragmentEvent::class.java)
        }
    }


    val negativeButtonClickListener = DialogInterface.OnClickListener { _, which ->
        // Code à exécuter si le bouton négatif est cliqué
        if (which == DialogInterface.BUTTON_NEGATIVE) {
            toastShow(requireContext(),"ok on ne touche à rien")
        }
    }

}


