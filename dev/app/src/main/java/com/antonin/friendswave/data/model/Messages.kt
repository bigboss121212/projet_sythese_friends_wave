package com.antonin.friendswave.data.model

import com.antonin.friendswave.adapter.ListItemViewModel

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: C'est une data class contenant toutes les informations pour d'un message

data class Messages (

    var message: String? = "",
    var senderId: String? = "",
    var senderName: String? = "",
    var timeStamp: String? = ""

    ): ListItemViewModel()


