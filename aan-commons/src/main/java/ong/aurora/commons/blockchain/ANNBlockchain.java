package ong.aurora.commons.blockchain;

import ong.aurora.commons.store.ANNEventStore;

import java.util.concurrent.CompletableFuture;

public class ANNBlockchain {

    ANNEventStore eventStore;

    public ANNBlockchain(ANNEventStore eventStore) {
        this.eventStore = eventStore;
    }

    public CompletableFuture<Void> verifyIntegrity() {


        this.eventStore.readEventStore().reduce((event, event2) -> {

            System.out.println("Verificando integridad");
            System.out.println(event);
            System.out.println(event2);

            return event2;
        });



        return CompletableFuture.completedFuture(null);
    }

    public boolean isEmpty() {
        return blockCount() == 0;
    }

    public long blockCount() {
        return this.eventStore.readEventStore().count();
    }
}
