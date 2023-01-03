package ong.aurora.commons.blockchain;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import ong.aurora.commons.event.Event;
import ong.aurora.commons.serialization.ANNSerializer;
import ong.aurora.commons.serialization.jackson.ANNJacksonSerializer;
import ong.aurora.commons.store.ANNEventStore;
import ong.aurora.commons.store.file.FileEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ANNBlockchain {

    ANNEventStore eventStore;

    ANNSerializer serializer;


    private static final Logger log = LoggerFactory.getLogger(ANNBlockchain.class);

    public ANNBlockchain(ANNEventStore eventStore, ANNSerializer annSerializer) {
        this.eventStore = eventStore;
        this.serializer = annSerializer;
    }

    public CompletableFuture<Void> verifyIntegrity() {


        eventStream().reduce((event, event2) -> {

            log.info("Verificando integridad \n{}\n{}", event, event2);
            if ((event.eventId()) != (event2.eventId() - 1)) {
                log.error("!! Cadena inválida {} != {}", event.eventId(), (event2.eventId() - 1));
                throw new RuntimeException("Blockchain inválida");
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

    public Stream<Event> eventStream() {
        return this.eventStore.readEventStore().map(s -> serializer.fromJSON(s, Event.class));
    }

    public Optional<Event> lastEvent() {
        return this.eventStream().max(Comparator.comparingLong(Event::eventId));
    }

    public Optional<String> lastEventHash() {
        return this.lastEvent().map(this::eventHash);
    }

    public CompletableFuture<Void> persistEvent(Event event) throws Exception {
        return this.eventStore.saveEvent(serializer.toJSON(event));
    }

    public String eventHash(Event event) {
        return Hashing.sha256().hashString(serializer.toJSON(event), StandardCharsets.UTF_8).toString();
    }
}
