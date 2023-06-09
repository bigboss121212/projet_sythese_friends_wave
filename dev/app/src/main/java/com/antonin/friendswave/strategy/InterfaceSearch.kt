package com.antonin.friendswave.strategy

import com.antonin.friendswave.data.model.Event
import com.antonin.friendswave.data.model.User

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: C'est l'interface permettant de déclarer des fonctions qui pourront s'implémenter dans d'autres classes
// ici on a une méthode pour trier les events

interface InterfaceSearch {

    fun sortedEvent(str : String, event: List<Event>?, user: User): List<Event>

}