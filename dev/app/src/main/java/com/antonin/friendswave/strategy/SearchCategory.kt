package com.antonin.friendswave.strategy

import com.antonin.friendswave.data.model.Event
import com.antonin.friendswave.data.model.User
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Les classes permettant d'implémenter les interfaces de strategies et definir chaque stratégie pour les events


// Refactoring guru pour le modele de la strategie et exemple https://asvid.github.io/kotlin-strategy-pattern :

class SearchCategory: InterfaceSearch {

    override fun sortedEvent(str: String, event: List<Event>?, user: User): List<Event> {
        val tempListEvent : ArrayList<Event> = ArrayList()

        for(data in event!!) {
            if(str.lowercase().contains(data.categorie!!.lowercase()) || data.categorie!!.lowercase().contains(str.lowercase())) {
                tempListEvent.add(data)
            }
        }
        return tempListEvent
    }
}

class SearchByName : InterfaceSearch {

    override fun sortedEvent(str: String, event: List<Event>?, user: User): List<Event> {
        val tempListEvent : ArrayList<Event> = ArrayList()

        for(data in event!!) {

            if(data.name!!.lowercase().contains(str.lowercase()) || data.description!!.lowercase().contains(str.lowercase())) {
                tempListEvent.add(data)
            }
        }
        return tempListEvent
    }
}

//Documentation pour la formule d'Haversine https://www.geeksforgeeks.org/haversine-formula-to-find-distance-between-two-points-on-a-sphere/

class SearchByCities : InterfaceSearch {

    override fun sortedEvent(str: String, events: List<Event>?, user: User): List<Event> {

        val foundEvents = mutableListOf<Event>()
        val earthRadius = 6371.0 // Rayon moyen de la Terre en kilomètres

        val radius = str.toInt()

        for (event in events!!) {
            val eventLatitude = event.lattitude!!.toDouble()
            val eventLongitude = event.longitude!!.toDouble()

            val dLat = Math.toRadians(eventLatitude - user.lattitude as Double)
            val dLon = Math.toRadians(eventLongitude - user.longitude as Double)

            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(user.lattitude as Double)) * cos(Math.toRadians(eventLatitude)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))

            val distance = earthRadius * c

            if (distance <= radius) {
                foundEvents.add(event)
            }
        }
        return foundEvents
    }

}

