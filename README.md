# FriendsWave


Ce projet est une application de réseau social développée en Kotlin. 
Il permet aux utilisateurs de partager des évents pour rencontrer de nouvelles personnes. 
L'utilisateur pourra remplir son profil, créer des évenements, ajouter des amis et communiquer entre eux.

Auteurs

Alexandre Caron

Antonin Lenoir

## Fonctionnalités

- Inscription et connexion des utilisateurs
- Publication d'evenements
- Chat entre utilisateurs
- Ajout et gestion des amis
- Notification pour les mises à jour et les activités des amis
- Recherche de personnes et d'évenements
- Personnalisation des profils utilisateurs
- Support de la messagerie instantanée
- Option de confidentialité pour les publications et les informations personnelles

## Prérequis

Android Studio Dolphin
Kotlin

## Installation

1. Clonez ce dépôt sur votre machine locale.

```bash
git clone https://github.com/Antonin555/FriendsWave.git
```

2. Ouvrez Android Studio.

3. Sélectionnez "File" -> "Open" et naviguez vers le répertoire `FriendWave` cloné précédemment.

4. Attendez que Gradle synchronise et télécharge les dépendances nécessaires.

5. Configurez votre projet Firebase :

   - Connectez-vous à la console Firebase (https://console.firebase.google.com/) et créez un nouveau projet.
   - Ajoutez une application Android à votre projet Firebase en suivant les instructions fournies.
   - Téléchargez le fichier de configuration `google-services.json` et placez-le dans le répertoire `app/` de votre projet Dolphin.

6. Exécutez l'application en cliquant sur le bouton "Run" dans Android Studio ou en utilisant la commande `./gradlew installDebug` dans le répertoire racine du projet.

7. Installer un emulateur

    - Assurez-vous d'avoir Android Studio installé sur votre système. Si ce n'est pas le cas, téléchargez-le depuis le site officiel d'Android Studio et suivez les instructions d'installation.

    - Ouvrez Android Studio et accédez au "SDK Manager". Vous pouvez le trouver dans la barre d'outils supérieure en cliquant sur "SDK Manager" ou en utilisant la fonction de recherche.

    - Dans le "SDK Manager", sélectionnez l'onglet "SDK Tools". Faites défiler la liste jusqu'à trouver "Android Emulator". Assurez-vous que la case à côté de "Android Emulator" est cochée, puis cliquez sur "OK" pour l'installer.

    - Une fois l'installation terminée, retournez à Android Studio et créez un nouveau projet Android ou ouvrez un projet existant.

    - Recherchez et ouvrez le "AVD Manager" dans la fenêtre principale d'Android Studio. Vous pouvez y accéder en cliquant sur l'icône de périphérique Android dans la barre d'outils supérieure ou en utilisant la fonction de recherche.

    - Dans l'AVD Manager, cliquez sur le bouton "Create Virtual Device" pour créer un nouvel appareil virtuel.

    - Sélectionnez le type de périphérique que vous souhaitez émuler, par exemple "Phone" ou "Tablet". Choisissez ensuite un appareil spécifique dans la liste et cliquez sur "Next".

    - Sélectionnez la version de l'API Android que vous souhaitez utiliser pour l'émulateur. Vous pouvez choisir une version existante ou télécharger une nouvelle version depuis la liste déroulante. Cliquez sur "Next".

    - Choisissez les paramètres de performance de l'appareil virtuel, tels que la quantité de RAM et la résolution de l'écran. Cliquez sur "Finish" pour terminer la création de l'appareil virtuel.

    - Une fois l'appareil virtuel créé, sélectionnez-le dans la liste des périphériques disponibles dans l'AVD Manager, puis cliquez sur le bouton "Play" pour démarrer l'émulateur.





## Installation Google Maps

1. Ajoutez la clé d'API Google Maps à votre projet :

- Accédez à la console des développeurs Google.
- Créez un nouveau projet ou sélectionnez un projet existant.
- Activez l'API Google Maps Android.
- Générez une clé d'API pour votre projet.

2. Ajoutez la dépendance Google Play Services dans votre fichier build.gradle (Module: app) :

implementation 'com.google.android.gms:play-services-maps:17.0.1'

3. Ajoutez l'autorisation d'accès à Internet dans votre fichier AndroidManifest.xml :

<uses-permission android:name="android.permission.INTERNET" />

4. Ajoutez le composant <meta-data> dans votre fichier AndroidManifest.xml, en remplaçant YOUR_API_KEY par votre clé d'API Google Maps :

<application ...>
    ...
    <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="YOUR_API_KEY" />
</application>

## Pour Firebase Storage

- Accédez à la console Firebase.
- Créez un nouveau projet Firebase ou sélectionnez un projet existant.
- Accédez à l'onglet "Storage" dans la console Firebase.
- Activez le service Firebase Storage pour votre projet.

## Pour Firebase Cloud Function

- Accédez à la console Firebase.
- Créez un nouveau projet Firebase ou sélectionnez un projet existant.
- Accédez à l'onglet "Functions" dans la console Firebase.
- Activez le service Firebase Cloud Functions pour votre projet.

Installez l'outil Firebase CLI :

- Installez Node.js sur votre machine.
- Ouvrez une invite de commandes ou un terminal.
- Exécutez la commande suivante pour installer l'outil Firebase CLI :

npm install -g firebase-tools

Connectez-vous à Firebase dans l'outil Firebase CLI :

- Dans l'invite de commandes ou le terminal, exécutez la commande suivante :

firebase login

Suivez les instructions pour vous connecter à votre compte Firebase.

## Utilisation

Commencez par lancer l'application sur android studio. Lors du démarrage de l'application, l'utilisateur est invité à se créer un compte, il doit donc se diriger vers la page registrée. Il doit remplir tous les champs avec des informations valides. Par la suite l'utilisateur a accès à toutes les fonctionnalités de l'application. Il peut donc aller chercher dans les événements publics afin de voir si l'un d'eux l'intéresse. Il peut lui-même créer un événement de sont choix, qu'il soit privé ou public. S'il connait déjà des utilisateurs, il peut leur faire une demande d'invitation à l'aide de leur courriel. Au courant de sont utilisation l'utilisateur recevra des notifications pour lui indiquer s’il y a du changement dans ces activités. 
 


## Ressources utiles

- [Documentation Android Studio](https://developer.android.com/studio/documentation)
- [Documentation Kotlin](https://kotlinlang.org/docs/home.html)
- [Documentation JetPack](https://developer.android.com/jetpack)
- [Documentation Firebase](https://firebase.google.com/docs)

