package ong.aurora.commons.blockchain;

import com.google.common.hash.Hashing;
import ong.aurora.commons.event.Event;
import ong.aurora.commons.serialization.AANSerializer;
import ong.aurora.commons.store.ANNEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.subjects.BehaviorSubject;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class AANBlockchain {

    ANNEventStore eventStore;

    AANSerializer serializer;

    public BehaviorSubject<Event> lastEventStream;


    private static final Logger log = LoggerFactory.getLogger(AANBlockchain.class);

    public AANBlockchain(ANNEventStore eventStore, AANSerializer annSerializer) {
        this.eventStore = eventStore;
        this.serializer = annSerializer;
        this.lastEventStream = BehaviorSubject.create(eventStream().max(Comparator.comparingLong(Event::eventId)).orElse(null));
    }

    public CompletableFuture<Void> verifyIntegrity() {
        log.info("Verificando integridad de blockchain");

        eventStream().reduce((event, event2) -> {

            log.debug("Verificando integridad \n{}\n{}", event, event2);
            if ((event.eventId()) != (event2.eventId() - 1)) {
                log.error("!! Cadena inválida {} != {}", event.eventId(), (event2.eventId() - 1));
                throw new RuntimeException("Blockchain inválida");
            }

            return event2;
        });

        log.info("Verificación completada correctamente");

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

//    public Optional<Event> lastEventt() {
//        return this.eventStream().max(Comparator.comparingLong(Event::eventId));
//    }

    public Optional<Event> lastEvent() {
        return Optional.of(this.lastEventStream.getValue());
    }



    public CompletableFuture<Void> persistEvent(Event event) throws Exception {
        // TODO COMPROBAR HASH
        this.eventStore.saveEvent(serializer.toJSON(event));
//        onEventPersisted.onNext(event);
        lastEventStream.onNext(event);
        return CompletableFuture.completedFuture(null);
    }

    public String nextBlockHash() { // TODO

        if (this.lastEvent().isEmpty()) {
            return "null";
        }

        return Hashing.sha256().hashString(serializer.toJSON(this), StandardCharsets.UTF_8).toString();
    }
}
