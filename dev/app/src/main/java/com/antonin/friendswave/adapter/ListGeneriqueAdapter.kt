package com.antonin.friendswave.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.antonin.friendswave.R
import com.antonin.friendswave.data.firebase.FirebaseStore

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: permet d'avoir un view Holder pour les recycler View de facon générique. Le but est de pouvoir afficher différentes données dans plusieurs recycler View.

// nous avons pris ce code pour s'inspirer https://proandroiddev.com/generic-listadapter-with-kotlin-write-once-use-more-recyclerview-viewpager-6314cbdced36
// nous l'avons adapter à notre code

class ListGeneriqueAdapter <T : ListItemViewModel>(@LayoutRes val layoutId: Int) :
        ListAdapter<T, ListGeneriqueAdapter.GenericViewHolder<T>>(WordsComparator()) {

        private val items = mutableListOf<T>()
        private val store = FirebaseStore()
        private var inflater: LayoutInflater? = null
        private var onListItemViewClickListener: OnListItemViewClickListener? = null

        @SuppressLint("NotifyDataSetChanged")
        fun addItems(items: List<T>) {
            this.items.clear()
            this.items.addAll(items)
            notifyDataSetChanged()
        }

        fun setOnListItemViewClickListener(onListItemViewClickListener: OnListItemViewClickListener?){
            this.onListItemViewClickListener = onListItemViewClickListener
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder<T> {
            val layoutInflater = inflater ?: LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, layoutId, parent, false)
            return GenericViewHolder(binding)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: GenericViewHolder<T>, position: Int) {
            val itemViewModel = items[position]

            itemViewModel.adapterPosition = position

            onListItemViewClickListener?.let { itemViewModel.onListItemViewClickListener = it
            }
            holder.bind(itemViewModel)

            val image_event = holder.itemView.findViewById<ImageView>(R.id.imageEvent)
            val image_profil = holder.itemView.findViewById<ImageView>(R.id.imageProfil)

            val image_profil_potential_guest = holder.itemView.findViewById<ImageView>(R.id.profil_potential_guest)

            if(image_profil != null){
                val path_profil = "photos/"+ itemViewModel.img.toString()
                store.displayImage(image_profil,path_profil)

            }

            if(image_event != null){
                val path_event = "photosEvent/" + itemViewModel.imgEvent.toString()
                store.displayImage(image_event,path_event)
            }

            if(image_profil_potential_guest != null){
                val path_profil = "photos/"+ itemViewModel.img.toString()
                store.displayImage(image_profil_potential_guest,path_profil)
            }

        }


    class GenericViewHolder<T : ListItemViewModel>(private val binding: ViewDataBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(itemViewModel: T) {
                binding.setVariable(BR.item, itemViewModel)
                binding.executePendingBindings()
            }
        }

        interface OnListItemViewClickListener{
            fun onClick(view: View, position: Int)
        }
    }


class WordsComparator <T : Any>: DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }
}



