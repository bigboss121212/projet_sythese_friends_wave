package com.antonin.friendswave.data.model

import com.antonin.friendswave.adapter.ListItemViewModel

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: C'est une data class contenant toutes les informations pour un event

data class Event(

    var key : String? = "",
    var name: String? = "",
    var public : Boolean? = true,
    var nbrePersonnes : Int? = 0,
    var admin:String = "",
    var categorie : String? = "",
    var date: String? = "",
    var heure: String? = "",
    var adress:String? = "",
    var description : String? = "",
    var lattitude : String? = "",
    var longitude : String? = "",
    var duree: Int? = 0,
    var pseudo:String? ="",
    var timeStamp: Double? = 0.0,
    var listInscrits : HashMap<String, String> = HashMap(),
    var invitations : HashMap<String, String> = HashMap(),
    var pendingRequestEventPublic : HashMap<String, String> = HashMap(),
    var nbreInscrit : Int? = 0,

    ):ListItemViewModel()