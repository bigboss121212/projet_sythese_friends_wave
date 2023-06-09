package com.antonin.friendswave.strategy

import android.os.Build
import androidx.annotation.RequiresApi
import com.antonin.friendswave.data.model.User
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

//Documentation sur Jaccard https://www.geeksforgeeks.org/find-the-jaccard-index-and-jaccard-distance-between-the-two-given-sets/

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Les classes permettant d'implémenter les interfaces de strategies et definir chaque stratégie pour les utilisateurs

class SearchHobbyFriend: InterfaceSearchFriend {
    // methode aidé par https://www.geeksforgeeks.org/find-the-jaccard-index-and-jaccard-distance-between-the-two-given-sets/
    override fun sortedUser(mainUser: User?, totalUser: List<User>?): List<User> {

        val similarUsers : ArrayList<User> = ArrayList()

        for (user in totalUser!!){
            if (user != mainUser){

                val interests1 = mainUser?.interet!!.toSet()
                val interests2 = user.interet!!.toSet()

                val intersection = interests1.intersect(interests2)
                val union = interests1.union(interests2)
                val similarity = intersection.size.toDouble() / union.size.toDouble()

                if(similarity >= 0.5){
                    similarUsers.add(user)
                }
            }
        }
        return similarUsers
    }

}

class SearchCityFriend: InterfaceSearchFriend {


    override fun sortedUser(mainUser: User?, totalUser: List<User>?): List<User> {
        val similarUsers : ArrayList<User> = ArrayList()

        for(user in totalUser!!){
            if(user != mainUser){
                if (user.lieu == mainUser!!.lieu){
                    similarUsers.add(user)
                }
            }
        }
        return similarUsers
    }

}

class SearchAgeFriend: InterfaceSearchFriend {


    @RequiresApi(Build.VERSION_CODES.O)
    override fun sortedUser(mainUser: User?, totalUser: List<User>?): List<User> {
        val similarUsers : ArrayList<User> = ArrayList()

        val mainUserAge = mainUser?.date?.let { calculateAge(it) }

        similarUsers.addAll(totalUser!!
            .filter { user ->
                val userAge = user.date?.let { calculateAge(it) }
                userAge != null && mainUserAge != null && userAge in (mainUserAge - 5)..(mainUserAge + 5)
            })

        return similarUsers

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateAge(dateOfBirth: String): Int {
        val dob = LocalDate.parse(dateOfBirth, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        val now = LocalDate.now()
        val age = Period.between(dob, now).years
        return age
    }

}