const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
admin.initializeApp();

const { CloudTasksClient } = require("@google-cloud/tasks");
const tasksClient = new CloudTasksClient();

// Konfiguracja 
const project = "gymbuddy2-4e8f6"; // np. "moj-projekt"
const location = "us-central1";      // region, w którym działają  funkcje (np. us-central1)
const queue = "WORKOUT-REMINDER";       // nazwa kolejki w Cloud Tasks
const serviceAccountEmail = "gymbuddy2-4e8f6@appspot.gserviceaccount.com"; // adres konta serwisowego

// Funkcja wywoływana przy utworzeniu dokumentu w kolekcji "reminders".
// Jeśli timeOfReminder jest w przyszłości, tworzy zadanie w Cloud Tasks.
// Jeśli czas już nadszedł, wysyła powiadomienie od razu.


exports.createReminderTask = onDocumentCreated("reminders/{reminderId}", async (event) => {
  const reminderData = event.data.data();
  console.log("Reminder data:", reminderData);

  const { fcmToken, timeOfReminder, workoutId, messageText } = reminderData;

  if (!timeOfReminder) {
    console.error("Pole timeOfReminder jest puste!");
    return;
  }

  const now = admin.firestore.Timestamp.now();

  if (timeOfReminder.seconds <= now.seconds) {
    // Jeśli przypomnienie już nadszedło – wysyłamy powiadomienie od razu
    const message = {
      token: fcmToken,
      notification: {
        title: "Reminder",
        body: "You will have new workout soon! Do not forget!",
      },
    };

    try {
      const response = await admin.messaging().send(message);
      console.log("Powiadomienie wysłane:", response);
    } catch (error) {
      console.error("Błąd przy wysyłaniu powiadomienia:", error);
    }
  } else {
    // Jeśli przypomnienie jest ustawione na przyszłość – tworzymy zadanie w Cloud Tasks
    const parent = tasksClient.queuePath(project, location, queue);

    // URL funkcji HTTP-trigger, którą Cloud Tasks wywoła.
    // Po wdrożeniu funkcji znajdziesz go w konsoli Firebase (Functions -> URL)
    const url = `https://sendschedulednotification-rr72wi2doq-uc.a.run.app`;

    // Przygotowanie danych, które zostaną przesłane do funkcji HTTP
    const taskPayload = {
      fcmToken,
      workoutId,
      message: `You will have new workout in ${messageText}! Do not forget!`,
    };

    const task = {
      httpRequest: {
        httpMethod: "POST",
        url,
        headers: { "Content-Type": "application/json" },
        body: Buffer.from(JSON.stringify(taskPayload)).toString("base64"),
        // Opcjonalnie – zabezpieczenie wywołania endpointu za pomocą OIDC
        oidcToken: {
          serviceAccountEmail,
        },
      },
      scheduleTime: {
        seconds: timeOfReminder.seconds,
        nanos: timeOfReminder.nanoseconds,
      },
    };

    try {
      const [response] = await tasksClient.createTask({ parent, task });
      console.log(`Task created: ${response.name}`);
    } catch (error) {
      console.error("Error scheduling task:", error);
    }
  }
});



// Funkcja HTTP-trigger, która zostanie wywołana przez Cloud Tasks w ustalonym czasie.
// Odbiera dane z zadania i wysyła powiadomienie FCM.
exports.sendScheduledNotification = onRequest(async (req, res) => {
  const payload = req.body;
  const { fcmToken, workoutId, message } = payload;

  if (!fcmToken) {
    res.status(400).send("Brak tokenu FCM.");
    return;
  }

  const messagePayload = {
    token: fcmToken,
    notification: {
      title: "Workout Reminder",
      body: message || "",
    },
  };

  try {
    const response = await admin.messaging().send(messagePayload);
    console.log("Powiadomienie wysłane:", response);
    res.status(200).send("Powiadomienie wysłane.");
  } catch (error) {
    console.error("Błąd przy wysyłaniu powiadomienia:", error);
    res.status(500).send("Błąd przy wysyłaniu powiadomienia.");
  }
});


