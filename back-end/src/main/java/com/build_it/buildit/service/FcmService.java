package com.build_it.buildit.service;

import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FcmService {

    // You will need to inject the repository to clean up dead tokens
    // private final DeviceTokenRepository deviceTokenRepository;

    public void sendPushNotificationToUser(List<String> targetTokens, String title, String body) {
        if (targetTokens == null || targetTokens.isEmpty()) {
            return;
        }

        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(targetTokens)
                .setNotification(notification)
                .putData("click_action", "FLUTTER_NOTIFICATION_CLICK")
                .build();

        try {
            // sendEachForMulticast is the modern way to send to multiple devices
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            // --- THE CLEANUP PHASE ---
            // If a user uninstalls the app, their token becomes dead.
            // Firebase tells you which tokens failed so you can delete them from Postgres!
            if (response.getFailureCount() > 0) {

                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        String failedToken = targetTokens.get(i);
                        String errorCode = responses.get(i).getException().getMessagingErrorCode().name();

                        if ("UNREGISTERED".equals(errorCode)) {
                            System.out.println("Dead token found, deleting: " + failedToken);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error sending FCM multicast message: " + e.getMessage());
        }
    }
}