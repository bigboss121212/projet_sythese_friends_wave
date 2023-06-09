package com.antonin.friendswave.strategy

import com.antonin.friendswave.data.model.Event
import com.antonin.friendswave.data.model.User

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: La classe qui va gérer l'ensemble des stratégies pour les events

class Strategy(private var interfaceSearch: InterfaceSearch) {

    fun search(str:String, list: List<Event>?, user : User) : List<Event> {
        return interfaceSearch.sortedEvent(str, list, user)
    }

}