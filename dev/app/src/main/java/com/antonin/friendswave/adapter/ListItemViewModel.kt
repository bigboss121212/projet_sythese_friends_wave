package com.antonin.friendswave.adapter

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Fait partie du ListAdapter Generique, permet de pouvoir cliquer sur les recycler et récupérer les infos adequates

abstract class ListItemViewModel{

    var adapterPosition: Int = -1
    var onListItemViewClickListener: ListGeneriqueAdapter.OnListItemViewClickListener? = null
    var img : String?= ""
    var imgEvent : String? = ""
    var imgCover : String? = ""
    var lastMessage: HashMap<String, String>? = HashMap()
}