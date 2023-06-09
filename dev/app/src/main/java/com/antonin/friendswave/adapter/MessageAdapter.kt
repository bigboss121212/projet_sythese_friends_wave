package com.antonin.friendswave.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.antonin.friendswave.R
import com.antonin.friendswave.data.model.Messages
import com.google.firebase.auth.FirebaseAuth

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Autre type d'adapter pour un recycler View pour avoir le format d'un Chat. Il permet de différencier les messages envoyés de ceux reçus.

//Documentation https://www.youtube.com/watch?v=8Pv96bvBJL4

class MessageAdapter(val context: Context, val messageList: List<Messages>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ITEM_RECEIVE = 1
    val ITEM_SENT = 2
    private val items = mutableListOf<Messages>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if(viewType == 1){
            val view: View = LayoutInflater.from(context).inflate(R.layout.receive, parent, false)
            return ReceiveViewHoler(view)

        }else{
            val view: View = LayoutInflater.from(context).inflate(R.layout.sent, parent, false)
            return SentViewHoler(view)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addItems(items: List<Messages>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val currentMessage = messageList[position]
        if(holder.javaClass == SentViewHoler::class.java){
            holder as SentViewHoler
            holder.sentMessage.text = currentMessage.message
            if(currentMessage.timeStamp != null) holder.sendTimeStamp.text = currentMessage.timeStamp
        }
        else{
            holder as ReceiveViewHoler
            holder.receiveMessage.text = currentMessage.message
            holder.reveiveUid.text = currentMessage.senderName + ":"
            if(currentMessage.timeStamp != null) holder.reveiveTimeStamp.text = currentMessage.timeStamp

        }
    }

    override fun getItemViewType(position: Int): Int {

        val currentMessage = messageList[position]

        if(FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderId)){
            return ITEM_SENT
        }
        else {
            return ITEM_RECEIVE
        }

    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    class SentViewHoler(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        val sentMessage = itemView.findViewById<TextView>(R.id.txt_sent_message)
        val sendTimeStamp = itemView.findViewById<TextView>(R.id.txt_send_timestamp)
    }

    class ReceiveViewHoler(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        val receiveMessage = itemView.findViewById<TextView>(R.id.txt_receive_message)
        val reveiveUid = itemView.findViewById<TextView>(R.id.txt_receive_uid)
        val reveiveTimeStamp = itemView.findViewById<TextView>(R.id.txt_receive_timestamp)
    }

}