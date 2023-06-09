package com.antonin.friendswave.ui.authentification

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Interface pour valider ou refuser l'authentification ou le login

interface InterfaceAuth {

    fun onSuccess()
    fun onFailure(message: String)
}