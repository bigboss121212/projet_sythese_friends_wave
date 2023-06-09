const functions = require('firebase-functions');
const admin = require('firebase-admin');
const capitalizeSentence = require('capitalize-sentence');
const badWordsList = require('french-badwords-list').array;

//Concernant les triggers functions https://github.com/firebase/functions-samples

admin.initializeApp(functions.config().firebase);

exports.moderatorEvent = functions.database.ref('/event/{eventId}').onWrite((change) => {
  const message = change.after.val();
  console.log(message.description)

  if (message && message.description && !message.sanitized) {
    functions.logger.log('Retrieved message content: ', message);
    
    const moderated_description = moderateMessage(message.description);
    const moderated_name_event = moderateMessage(message.name)
    

    functions.logger.log('Message has been moderated. Saving to DB: ', moderated_description);
    return change.after.ref.update({
      description: moderated_description,
      name:moderated_name_event,
      sanitized: true,
      moderated: message.description !== moderated_description,
    });
  }
  return null;
});



exports.moderatorUser = functions.database.ref('/user/{userId}').onWrite((change) => {
  const message = change.after.val();
  console.log(message.pseudo)

  if (message && message.pseudo && !message.sanitized) {
    functions.logger.log('Retrieved message content: ', message);
    
    const moderated_pseudo = moderateMessage(message.pseudo);

    

    functions.logger.log('Message has been moderated. Saving to DB: ', moderated_pseudo);
    return change.after.ref.update({
      pseudo: moderated_pseudo,
      sanitized: true,
      moderated: message.description !== moderated_pseudo,
    });
  }
  return null;
});


function moderateMessage(message) {
  if (containsSwearwords(message)) {
    functions.logger.log('User is swearing. Moderating...');
    message = moderateSwearwords(message);
  }
  return message;
}

function containsSwearwords(message) {
  const cleanedMessage = message.replace(new RegExp(badWordsList.join('|'), 'gi'), '***');
  return cleanedMessage !== message;
}

function moderateSwearwords(message) {
  return message.replace(new RegExp(badWordsList.join('|'), 'gi'), '***');
}



// Envoie une notification a l'admin de l'event lorsqu'il recoit une demande

exports.notifHostSubscribeEventPublic = functions.database.ref(`/user/{userid}/hostPendingRequestEventPublic`).onUpdate((change, context) => {
  const host_token_ref = admin.database().ref(`/user/${context.params.userid}/token`);

  return host_token_ref.once('value').then((snapshot) => {
    const host_token = snapshot.val();

    console.log("Le token de l'administrateur : " + host_token);

    const payload = {
      notification: {
        title: 'Someone wants to participate in your event',
        body: 'Check your notifications'
      }
    };

    return admin.messaging().sendToDevice(host_token, payload);
  });
});

// envoie une notif lorsqu'on recoit une demande d'ami, 
// A tester a distance

exports.sendNotifFriendRequest = functions.database.ref(`/user/{userid}/friendRequest`).onUpdate((change, context) => {

  const host_token_ref = admin.database().ref(`/user/${context.params.userid}/token`);

  return host_token_ref.once('value').then((snapshot) => {
    const host_token = snapshot.val();

    console.log("Le token de l'administrateur : " + host_token);

    const payload = {
      notification: {
        title: 'Someone wants add you as friend',
        body: 'Check your notifications'
      }
    };

    return admin.messaging().sendToDevice(host_token, payload);
  });



});

// Fonctionne
// ENVOIE UNE NOTIF LORSQU'UN NOUVEAU INNSCRIT REJOINT L'EVENT ou le quitte ou tout changement dans ListInscrit

exports.sendNotificationToSpecificUser = functions.database.ref(`/event/{eventId}`)
    .onUpdate((snapshot, context) => {
        const child_mail = [];

        const listInscritsRef = admin.database().ref(`/event/${context.params.eventId}/listInscrits`);

        listInscritsRef.once('value', (snapshot) => {
            snapshot.forEach((childSnapshot) => {
                child_mail.push(childSnapshot.val());
            });
        }).then(() => {
            // Get the device tokens for the recipients
            const promises = [];
            child_mail.forEach(email => {
                const userRef = admin.database().ref('/user').orderByChild('email').equalTo(email);
                const promise = userRef.once('value').then((snapshot) => {
                    snapshot.forEach((childSnapshot) => {
                        const recipientDeviceToken = childSnapshot.child('token').val();

                        // Send the notification
                        const payload = {
                            data: {
                                title: 'Un nouveau participant pour votre event',
                                body: 'Regardez votre evenement',
                                click_action: 'HOME_FRAGMENT'
                            }
                        };
                        promises.push(admin.messaging().sendToDevice(recipientDeviceToken, payload));
                    });
                });
                promises.push(promise);
            });

            return Promise.all(promises);
        });
    });




    // Envoi une notif a tous les users lorsqu'un event Public est crée 
    exports.sendNotification = functions.database.ref('/event/{eventPublicId}').onCreate(async (change, context) => {
        const eventData = change.after.val();
        const topic = "notif_event_public";
        const postData = change.after.val(); //3
      
      
        const message = {
  
            data: {
                title: "TTTTTTTTTTTTTEEEEST 2222 ",
                body:"Decouvrez l'evenement : " + postData.name,
                message: "HEEEEEEEEEEEEEEEEEEEEEEEEEEEE",
                click_action: "HOME_ACTIVITY"
              }


        };
        try {
            const response = await admin.messaging().sendToTopic(topic, message);
            console.log("Successfully sent message:", message);
        } catch (error) {
            console.log("Error sending message:", error);
        }
    });
    

exports.scheduledFunction = functions.pubsub
    .schedule('every 24 hours')
    .onRun((context) => {
      deleteOldItems();
      return null;
    });
  
  function deleteOldItems() {
    const ref = admin.database().ref('/event');
    const now = Date.now();
    const date_expiration = now + (96 * 3600 * 1000);
    const oldItemsQuery = ref.orderByChild('timeStamp').endAt(date_expiration);
    oldItemsQuery.once('value', function(snapshot) {
      const updates = {};
      snapshot.forEach(function(child) {
        updates[child.key] = null;
      });
      ref.update(updates);
    });
  }





// envoyer vers rating activity lorsque l'event est terminé :
    exports.checkExpiredEvents = functions.pubsub.schedule('every 24 hours').onRun((context) => {
      const ref = admin.database().ref('/event');
      const now = Date.now();
    
      const dateExpiration = now;
      const expiredEventsQuery = ref.orderByChild('timeStamp').endAt(dateExpiration);
    
      return expiredEventsQuery.once('value').then((snapshot) => {
        const promises = [];
    
        snapshot.forEach((childSnapshot) => {
          const eventId = childSnapshot.key;
          const eventData = childSnapshot.val();
    
          // Check if the event is expired
          if (eventData && eventData.timeStamp <= now) {
            const listInscritsRef = admin.database().ref(`/event/${eventId}/listInscrits`);
    
            const promise = listInscritsRef.once('value').then((inscritsSnapshot) => {
              const notificationPromises = [];
    
              inscritsSnapshot.forEach((inscritSnapshot) => {
                const email = inscritSnapshot.val();
                const userRef = admin.database().ref('/user').orderByChild('email').equalTo(email);
    
                const notificationPromise = userRef.once('value').then((userSnapshot) => {
                  userSnapshot.forEach((userChildSnapshot) => {
                    const recipientDeviceToken = userChildSnapshot.child('token').val();
    
                    // Send the notification
                    const payload = {
                      data: {
                        title: 'Vous avez participé un evenement, notez le',
                        body: 'Cliquez pour noter votre dernier event',
                        click_action: 'HOME_FRAGMENT'
                      }
                    };
    
                    notificationPromises.push(admin.messaging().sendToDevice(recipientDeviceToken, payload));
                  });
                });
    
                notificationPromises.push(notificationPromise);
              });
    
              return Promise.all(notificationPromises);
            });
    
            promises.push(promise);
          }
        });
    
        return Promise.all(promises);
      });
    });
    
  