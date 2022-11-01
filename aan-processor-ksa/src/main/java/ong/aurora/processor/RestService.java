package ong.aurora.processor;

import io.javalin.Javalin;
import io.javalin.http.Context;
import ong.aurora.commons.event.Event;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

class RestService {
  private final HostInfo hostInfo;
  private final KafkaStreams streams;

  private static final Logger log = LoggerFactory.getLogger(RestService.class);

  RestService(HostInfo hostInfo, KafkaStreams streams) {
    this.hostInfo = hostInfo;
    this.streams = streams;
  }

  ReadOnlyKeyValueStore<String, Event> getEventStore() {
    return streams.store(
        StoreQueryParameters.fromNameAndType(
            "aan-events-store", QueryableStoreTypes.keyValueStore()));
  }

  void start() {
    Javalin app = Javalin.create().start(hostInfo.port());
    app.get("/event/all", this::eventAll);
  }

  void eventAll(Context context) {

    List<Event> eventList = new ArrayList<>();

    try (KeyValueIterator<String, Event> iterator = this.getEventStore().all()) {
      while (iterator.hasNext()) {
        KeyValue<String, Event> keyValue = iterator.next();
        eventList.add(keyValue.value);
      }
    }

    context.json(eventList);

  }

}
