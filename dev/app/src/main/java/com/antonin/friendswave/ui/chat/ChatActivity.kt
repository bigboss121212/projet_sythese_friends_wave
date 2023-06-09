package com.antonin.friendswave.ui.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.antonin.friendswave.R
import com.antonin.friendswave.adapter.MessageAdapter
import com.antonin.friendswave.data.firebase.FirebaseSourceEvent
import com.antonin.friendswave.data.firebase.FirebaseSourceUser
import com.antonin.friendswave.data.repository.EventRepo
import com.antonin.friendswave.data.repository.UserRepo
import com.antonin.friendswave.databinding.ActivityChatBinding
import com.antonin.friendswave.ui.viewModel.ChatViewModel
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import com.antonin.friendswave.ui.viewModel.ChatVMFactory

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Activité qui permet de chatter

//Tutoriel pour faire le chat: https://www.youtube.com/watch?v=8Pv96bvBJL4

class ChatActivity : AppCompatActivity(), KodeinAware {

    override val kodein : Kodein by kodein()
    private val factory : ChatVMFactory by instance()
    private var viewModel : ChatViewModel = ChatViewModel(repository = UserRepo(firebaseUser= FirebaseSourceUser()),
    repoEvent = EventRepo(firebaseEvent = FirebaseSourceEvent()))
    private  lateinit var messageAdapter : MessageAdapter
    private lateinit var binding : ActivityChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val reveiverUid = intent.getStringExtra("uid")
        viewModel = ViewModelProviders.of(this,factory)[ChatViewModel::class.java]
        viewModel.receiverUid = reveiverUid
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        binding.chatRecyclerView.layoutManager = layoutManager

        viewModel.messageList.observe(this) { messageList ->
            messageAdapter = MessageAdapter(this, messageList)
            messageAdapter.addItems(messageList)
            binding.chatRecyclerView.adapter = messageAdapter
        }

        viewModel.message.observe(this) { message ->
            binding.messageBox.setText(message)
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchUserData()
        viewModel.fetchDiscussion()
    }

}