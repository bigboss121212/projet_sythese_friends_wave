package com.antonin.friendswave.ui.fragmentMain

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.antonin.friendswave.R
import com.antonin.friendswave.adapter.ListGeneriqueAdapter
import com.antonin.friendswave.data.firebase.FirebaseSourceUser
import com.antonin.friendswave.data.model.User
import com.antonin.friendswave.data.repository.UserRepo
import com.antonin.friendswave.databinding.FragmentContactBinding
import com.antonin.friendswave.outils.goToActivityWithArgs
import com.antonin.friendswave.ui.chat.ChatActivity
import com.antonin.friendswave.ui.home.ProfilActivity
import com.antonin.friendswave.ui.viewModel.HomeFragmentVMFactory
import com.antonin.friendswave.ui.viewModel.HomeFragmentViewModel
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Fragment representant la Vue pour voir ses contacts et cliquer dessus pour chatter ou voir leurs profils

class ContactFragment : Fragment(), KodeinAware {

    override val kodein : Kodein by kodein()
    private val factory : HomeFragmentVMFactory by instance()
    private lateinit var adapter1 : ListGeneriqueAdapter<User>
    private var viewModel: HomeFragmentViewModel  = HomeFragmentViewModel(repository = UserRepo(firebaseUser = FirebaseSourceUser()))
    private lateinit var binding: FragmentContactBinding
    private val REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding  = DataBindingUtil.inflate(inflater, R.layout.fragment_contact, container, false)
        viewModel = ViewModelProviders.of(this,factory).get(HomeFragmentViewModel::class.java)
        binding.viewmodel = viewModel
        return binding.root
    }

    @SuppressLint("ResourceType")
    override fun onResume() {
        super.onResume()

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION_READ_EXTERNAL_STORAGE)
        }

        adapter1 = ListGeneriqueAdapter(R.layout.recycler_contact)
        val layoutManager = LinearLayoutManager(context)
        binding.recyclerFragmentContact.layoutManager = layoutManager
        binding.recyclerFragmentContact.adapter = adapter1

        viewModel.fetchUsersFriends()

        viewModel.emailUserList.observe(this) { userList ->
            adapter1.addItems(userList)
            if(userList.isEmpty()){
                binding.recoContact.visibility = View.VISIBLE
                binding.contactImg.visibility = View.VISIBLE

            }else{
                binding.recoContact.visibility = View.GONE
                binding.contactImg.visibility = View.GONE
            }

        }

        adapter1.setOnListItemViewClickListener(object : ListGeneriqueAdapter.OnListItemViewClickListener {
            override fun onClick(view: View, position: Int) {
                val userChoisi = binding.viewmodel!!.emailUserList.value?.get(position)

                if(view.id == R.id.imageProfil){
                    goToActivityWithArgs(view.context,ProfilActivity::class.java,"uid" to userChoisi?.uid.toString())

                } else {
                    goToActivityWithArgs(view.context,ChatActivity::class.java,"uid" to userChoisi?.uid.toString())
                }
            }
        })
    }

}

