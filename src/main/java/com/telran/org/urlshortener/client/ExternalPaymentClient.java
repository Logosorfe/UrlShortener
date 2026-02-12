package com.telran.org.urlshortener.client;

import org.springframework.stereotype.Component;

@Component
public class ExternalPaymentClient {
    public boolean waitForPayment(long subscriptionId) {
        for (int i = 0; i < 60; i++) {
            boolean isPaid = checkPaymentStatus(subscriptionId);
            if (isPaid) return true;
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    private boolean checkPaymentStatus(long id) {
        // TODO: call external API
        return false;
    }
}