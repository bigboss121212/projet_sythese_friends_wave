package com.antonin.friendswave.ui.viewModel

import android.content.Intent
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.antonin.friendswave.outils.emailRegex
import com.antonin.friendswave.data.repository.UserRepo
import com.antonin.friendswave.ui.authentification.InterfaceAuth
import com.antonin.friendswave.ui.authentification.LoginActivity
import com.antonin.friendswave.ui.authentification.SignupActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

//Auteur: Alexandre Caron et Antonin Lenoir
//Contexte: Classe permettant de gerer le ViewModel pour l'authentification et le login

// Methode du Completable : http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Completable.html

class AuthViewModel(private val repository: UserRepo) : ViewModel() {

    var name: String? = null
    var familyName: String? = null
    var nickname: String? = null
    var age: Int? = 0
    var day: Int? = 0
    var month: Int? = 0
    var year:Int? = 0
    var date: String? = ""
    var email: String? = null
    var city: String? = null
    var password: String? = null
    val toastMessage = MutableLiveData<String>()
    var interfaceAuth : InterfaceAuth? = null
    private val disposables = CompositeDisposable()
    private val _pseudoList = MutableLiveData<List<String>>()
    var pseudoList_live: LiveData<List<String>> = _pseudoList

    private val _emailList = MutableLiveData<List<String>>()
    val emailList: LiveData<List<String>> = _emailList

    val user by lazy {
        repository.currentUser()
    }

    fun goToSignup(view: View){
        // .also permet d'eviter de déclarer une variable :
        Intent(view.context, SignupActivity::class.java).also {
            view.context.startActivity(it)
        }
    }

    fun goToLogin(view: View){
        // .also permet d'eviter de déclarer une variable :
        Intent(view.context, LoginActivity::class.java).also {
            view.context.startActivity(it)
        }
    }

    fun login() {
        //validating email and password
        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            toastMessage.value = "Mauvais courriel ou mot de passe"
            interfaceAuth?.onFailure("Mauvais courriel ou mot de passe")
            return
        }

        repository.login(email!!, password!!)

        val disposable = repository.login(email!!, password!!).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
            interfaceAuth?.onSuccess()
        }, {
            interfaceAuth?.onFailure("Mauvais courriel ou mot de passe")
        })
        disposables.add(disposable)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun signup() {
        if (email.isNullOrEmpty() || password.isNullOrEmpty() || name.isNullOrEmpty() || familyName.isNullOrEmpty() || nickname.isNullOrEmpty() || city.isNullOrEmpty()) {
            interfaceAuth?.onFailure("Veuillez remplir tous les champs")
            return
        }
        if (!pseudoList_live.value.isNullOrEmpty()){
            if(pseudoList_live.value!!.contains(nickname!!)){
                interfaceAuth?.onFailure("Pseudo déjà utilisé, choisissez en un autre")
                return
            }
        }

        if(!verificatioAge(date!!)){
            interfaceAuth?.onFailure("Désolé pour utiliser cette application vous devez être majeur")
            return
        }
        if(!isEmailValid(email!!)){
            interfaceAuth?.onFailure("Désolé votre email n'est pas valide")
            return
        }
        if(!emailList.value.isNullOrEmpty()){
            if(emailList.value!!.contains(email!!)){
                interfaceAuth?.onFailure("Désolé, il existe deja un compte avec cette adresse mail, avez vous oublié votre mot de passe?")
                return
            }
        }

        val disposable = repository.register(name!!,email!!, password!!, familyName!!, nickname!!, city!!, date!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                interfaceAuth?.onSuccess()
            }, {
                interfaceAuth?.onFailure(it.message!!)
            })

        disposables.add(disposable)
    }

    //disposing the disposables
    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

    fun fetchAllPseudo(){
        repository.fetchAllPseudo().observeForever { pseudo ->
            _pseudoList.value = pseudo
        }
    }

    fun fetchEmail() {
        repository.fetchfetchEmail().observeForever{ email ->
            _emailList.value = email
        }
    }

    fun changeDate(year: Int, month: Int, day: Int) {

        val dayString =  if (day < 10) "0$day" else day.toString()
        val monthString = if (month <= 10) "0${month + 1}" else "${month + 1}"
        date = dayString + "/"+monthString +"/"+ year.toString()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun verificatioAge(date: String): Boolean {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val dateNaissance = LocalDate.parse(date, formatter)
        val age = ChronoUnit.YEARS.between(dateNaissance, LocalDate.now())
        return age >= 18 && dateNaissance.isBefore(LocalDate.now())
    }

    fun isEmailValid(email: String): Boolean {
        return emailRegex.matches(email)
    }
}

