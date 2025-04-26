import * as functions from "firebase-functions/v1";
import * as admin from "firebase-admin";

admin.initializeApp();

// Use v1 namespace explicitly to access document()
const firestore = functions.firestore;

/**
 * Cloud Function that watches for new notifications in Firestore
 * and sends them to the appropriate recipients via FCM
 */
export const sendNotification = firestore
  .document("notifications/{notificationId}")
  .onCreate(async (snapshot: functions.firestore.QueryDocumentSnapshot,
    context: functions.EventContext) => {
    const notificationId = context.params.notificationId;
    const notificationData = snapshot.data();

    // Log the incoming notification request
    console.log(`Processing notification ${notificationId}:`, notificationData);

    try {
      // Extract required fields
      const token = notificationData.token;
      if (!token) {
        throw new Error("No FCM token provided");
      }

      // Prepare FCM message
      const message = {

        // Add data payload (for custom handling in the app)
        data: {
          title: notificationData.title || "New notification",
          body: notificationData.body || "",
          requestId: notificationData.requestId || "",
          dayNumber: notificationData.dayNumber || "",
          type: notificationData.type || "general",
          testerId: notificationData.testerId || "",
          channelId: notificationData.channelId || "default",
          timestamp: admin.firestore.FieldValue.serverTimestamp().toString()
        },

        // The device token to send to
        token: token,
      };

      // Send the message through FCM
      const response = await admin.messaging().send(message);
      console.log(
        `Successfully sent notification ${notificationId}:`,
        response);

      // Update the notification document with sent status
      return snapshot.ref.update({
        status: "sent",
        sentAt: admin.firestore.FieldValue.serverTimestamp(),
      });
    } catch (error: unknown) {
      console.error(`Error sending notification ${notificationId}:`, error);

      // Update the notification document with error status
      return snapshot.ref.update({
        status: "error",
        error: error instanceof Error ? error.message : "Unknown error",
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
    }
  });
