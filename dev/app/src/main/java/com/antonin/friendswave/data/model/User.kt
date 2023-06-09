package com.antonin.friendswave.data.model

import com.antonin.friendswave.adapter.ListItemViewModel

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: C'est une data class contenant toutes les informations pour un utilisateur

data class User (
    var name: String? = "",
    var email: String? = "",
    var uid: String? = "",
    var familyName : String? = "",
    var nickname : String? = "",
    var lieu: String? = "",
//    var age: Int = 0,
    var date : String? = "",
    var desciption: String? = "",
    var langue: String? = "Francais",
    var etude: String? = "Self-Made",
    var rating: Double? =0.0,
    var friends : Int? = 0,
    var interet : ArrayList<String>? = ArrayList(),
    var friendRequest: HashMap<String, String>? = HashMap(),
    var friendList: HashMap<String, String>? = HashMap(),
    var invitations: HashMap<String,String>? = HashMap(),
    var eventConfirmationList: HashMap<String,String>? = HashMap(),
    var pendingRequestEventPublic: HashMap<String,String>? = HashMap(),
    var ConfirmHostRequestEventPublic: HashMap<String,String>? = HashMap(),
    var nbre_event : Int? =0,
    var lattitude : Double? = 0.0,
    var longitude : Double? = 0.0,

    ): ListItemViewModel()
