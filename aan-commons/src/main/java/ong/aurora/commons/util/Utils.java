package ong.aurora.commons.util;

import rx.Subscription;

public class Utils {

    public static void maybeUnsubscribe(Subscription subscription) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}
