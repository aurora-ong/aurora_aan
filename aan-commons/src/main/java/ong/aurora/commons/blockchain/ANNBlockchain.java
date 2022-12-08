package ong.aurora.commons.blockchain;

import ong.aurora.commons.store.ANNEventStore;
import ong.aurora.commons.store.file.FileEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class ANNBlockchain {

    ANNEventStore eventStore;

    private static final Logger log = LoggerFactory.getLogger(ANNBlockchain.class);

    public ANNBlockchain(ANNEventStore eventStore) {
        this.eventStore = eventStore;
    }

    public CompletableFuture<Void> verifyIntegrity() {


        this.eventStore.readEventStore().reduce((event, event2) -> {

            log.info("Verificando integridad \n{}\n{}", event, event2);
            if ((event.eventId()) != (event2.eventId() - 1)) {
                log.error("!! Cadena inválida {} != {}", event.eventId(), (event2.eventId() - 1));
            }

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
