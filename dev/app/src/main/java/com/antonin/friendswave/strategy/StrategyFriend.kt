package com.antonin.friendswave.strategy

import com.antonin.friendswave.data.model.User

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: La classe qui va gérer l'ensemble des stratégies pour les utilisateurs

class StrategyFriend(private var interfaceSearchFriend : InterfaceSearchFriend) {

    fun search(user: User?, list: List<User>?) : List<User> {
        return interfaceSearchFriend.sortedUser(user, list)
    }
}