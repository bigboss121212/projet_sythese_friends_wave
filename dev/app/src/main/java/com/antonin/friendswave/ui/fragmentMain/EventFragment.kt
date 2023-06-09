package com.antonin.friendswave.ui.fragmentMain

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.isDigitsOnly
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
import com.antonin.friendswave.databinding.FragmentEventBinding
import com.antonin.friendswave.strategy.SearchByCities
import com.antonin.friendswave.strategy.SearchByName
import com.antonin.friendswave.strategy.SearchCategory
import com.antonin.friendswave.strategy.Strategy
import com.antonin.friendswave.ui.event.DetailEventActivity
import com.antonin.friendswave.ui.event.GoogleLocation
import com.antonin.friendswave.ui.viewModel.EventFragmentVMFactory
import com.antonin.friendswave.ui.viewModel.EventFragmentViewModel
import com.google.android.gms.maps.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import com.antonin.friendswave.data.dataStructure.LinkedList
import com.antonin.friendswave.outils.goToActivityWithArgs
import com.antonin.friendswave.outils.toastShow

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Fragment permettant de voir les events publics

// Methodes trouvés sur les exemples de  Google Maps https://developers.google.com/maps/documentation/android-sdk/start?hl=fr

class EventFragment : Fragment(), KodeinAware, OnMapReadyCallback, LocationListener {

    override val kodein : Kodein by kodein()
    private val factory : EventFragmentVMFactory by instance()
    private lateinit var binding : FragmentEventBinding
    private var viewModel: EventFragmentViewModel = EventFragmentViewModel(
        repository = UserRepo(firebaseUser = FirebaseSourceUser()),
        repoEvent = EventRepo(firebaseEvent = FirebaseSourceEvent()))
    private var adapter1 : ListGeneriqueAdapter<Event> = ListGeneriqueAdapter(R.layout.recycler_events)
    private lateinit var loc : GoogleLocation
    private lateinit var googleMap: GoogleMap
    private var linkedList: LinkedList = LinkedList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loc = GoogleLocation()
        viewModel = ViewModelProviders.of(this,factory)[EventFragmentViewModel::class.java]
        viewModel.fetchEvents()
        viewModel.fetchUserData()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding  = DataBindingUtil.inflate(inflater, R.layout.fragment_event, container, false)
        binding.viewmodel = viewModel
        binding.mapView.getMapAsync(this)
        binding.mapView.onCreate(savedInstanceState)
        binding.lifecycleOwner = this

        return binding.root
    }

    @SuppressLint("ResourceType")
    override fun onResume() {
        binding.mapView.onResume()
        super.onResume()

        viewModel.eventList.observe(this){ eventList ->
            adapter1.addItems(eventList)
            if(eventList.isEmpty()){
                binding.noResultFound.visibility = View.VISIBLE

            }else{
                binding.noResultFound.visibility = View.GONE
            }
            for (i in eventList){
                linkedList.add(i)
            }
        }

        val searchCategory = SearchCategory()
        val searchByCities = SearchByCities()
        val searchByName = SearchByName()

        val layoutManager = LinearLayoutManager(context)
        binding.recyclerFragmentEvent.layoutManager = layoutManager
        binding.recyclerFragmentEvent.adapter = adapter1

        binding.btnRecherche.setOnClickListener{
            var searchStrategy = Strategy(searchCategory)
            val type = viewModel.strCategory.value.toString()

            if(viewModel.categorie == "(km) Autour de toi"){
                if(!type.isDigitsOnly()){
                    toastShow(context,"Vous devez inscrire un nombre")
                    return@setOnClickListener
                }
                searchStrategy = Strategy(searchByCities)
            }
            //A CHANGER IL N'Y A PAS D'INTERET DANS LES EVENTS, METTRE DATE?
            else if(viewModel.categorie == "Nom"){
                searchStrategy = Strategy(searchByName)
            }
            else if(viewModel.categorie == "Categorie"){
                searchStrategy = Strategy(searchCategory)
            }

            viewModel.strCategory.value = ""
            strategyEvent(searchStrategy,type)
        }

        adapter1.setOnListItemViewClickListener(object : ListGeneriqueAdapter.OnListItemViewClickListener{
            override fun onClick(view: View, position: Int) {

                val id_event = viewModel.eventList.value!!.get(position).key
                val admin_event = viewModel.eventList.value!!.get(position).admin

                goToActivityWithArgs(view.context,DetailEventActivity::class.java,
                    "position" to position,
                    "idEvent" to id_event.toString(),
                    "adminEvent" to admin_event)
            }
        })
    }

    fun strategyEvent(strategy: Strategy, str:String) {

        var tempList: ArrayList<Event>

        viewModel.eventList.observe(this){ eventList ->
            val user = viewModel.user_live.value
            tempList = strategy.search(str, eventList, user!!) as ArrayList<Event>

            if(tempList.isEmpty()){
                binding.noResultFound.visibility = View.VISIBLE

            }else{
                binding.noResultFound.visibility = View.GONE
            }
            adapter1.addItems(tempList)

            adapter1.setOnListItemViewClickListener(object : ListGeneriqueAdapter.OnListItemViewClickListener{
                override fun onClick(view: View, position: Int) {

                    if (tempList.size >= position){
                        val id_event = tempList.get(position).key
                        val admin_event = tempList.get(position).admin

                        goToActivityWithArgs(view.context,DetailEventActivity::class.java,
                            "position" to position,
                            "idEvent" to id_event.toString(),
                            "adminEvent" to admin_event)
                    }
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        viewModel.eventList.observe(requireActivity()){ eventList ->

            loc.getAllLocationsEvent(googleMap,eventList,binding.mapView, requireActivity(),requireContext())
        }
    }

    override fun onLocationChanged(p0: Location) {
        TODO("Not yet implemented")
    }
}
