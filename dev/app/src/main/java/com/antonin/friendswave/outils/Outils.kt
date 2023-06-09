package com.antonin.friendswave.outils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: C'est un fichier permettant de regrouper les actions ou les verifications communes à toutes les activités. Exemple passer d'une activité à une autre.

fun goToActivityWithoutArgs(context: Context, activityClass: Class<*>) {
    val intent = Intent(context, activityClass)
    context.startActivity(intent)
}

// Méthode trouvé avec chatGPT :
fun goToActivityWithArgs(context: Context?, activityClass: Class<*>, vararg arguments: Pair<String, Any>) {
    val intent = Intent(context, activityClass)
    for (argument in arguments) {
        val (clé, valeur) = argument
        when (valeur) {
            is String -> intent.putExtra(clé, valeur)
            is Int -> intent.putExtra(clé, valeur)
            is Boolean -> intent.putExtra(clé, valeur )
            // Ajoutez d'autres types de données selon vos besoins
        }
    }
    context!!.startActivity(intent)
}

fun toastShow(context: Context?,message:String) = Toast.makeText(context,message,Toast.LENGTH_SHORT).show()

val patternDate = Regex("\\d{2}/\\d{2}/\\d{4}")
val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")

//Documentation https://gist.github.com/BlackthornYugen/1b3e1ff4426294e7054c9a7190e8f2cd

fun sendEmail(recipient: String, name:String, email:String) {
    val props = Properties()
    props.setProperty("mail.smtp.host", "smtp-mail.outlook.com")
    props.setProperty("mail.smtp.port", "587")
    props.setProperty("mail.smtp.auth", "true")
    props.setProperty("mail.smtp.starttls.enable", "true")
    props.setProperty("mail.smtp.ssl.trust", "smtp-mail.outlook.com")

    val session = Session.getInstance(props, object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication("FriendWaveOfficial@hotmail.com", "Friend12Wave12")
        }
    })

    val message = MimeMessage(session)
    message.setFrom(InternetAddress("FriendWaveOfficial@hotmail.com"))
    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient))
    message.subject = "Venez rejoindre la communaute FriendsWave"
    message.setText("Bonjour " + email + " mieux connu(e) sous le nom de " +  name + " vous invite a rejoindre l'application FriendsWave")

    Thread(Runnable {
        try {
            Transport.send(message)
        } catch (e: MessagingException) {
            e.printStackTrace()
        }
    }).start()
}





