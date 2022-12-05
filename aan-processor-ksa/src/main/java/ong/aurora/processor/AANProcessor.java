package ong.aurora.processor;

import com.google.common.hash.Hashing;
import ong.aurora.commons.command.*;
import ong.aurora.commons.database.AANDatabase;
import ong.aurora.commons.event.Event;
import ong.aurora.commons.event.EventData;
import ong.aurora.commons.store.ANNEventStore;
import ong.aurora.commons.model.AANModel;
import ong.aurora.commons.projector.AANProjector;
import ong.aurora.model.v_0_0_1.AuroraOM;
import ong.aurora.processor.database.FileDatabase;
import ong.aurora.processor.projector.KSAProjector;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

public class AANProcessor implements Processor<String, Command, String, Event> {

    private static final Logger logger = LoggerFactory.getLogger(AANProcessor.class);

    private ProcessorContext<String, Event> context;

    private KeyValueStore<String, Event> kvStore;

    private Integer currentOffset;
    private String currentHash;

    private final AANDatabase aanDatabase = new FileDatabase();

    private AANProjector aanProjector;

    AANModel aanModel;
    ANNEventStore eventStore;

    public AANProcessor(ANNEventStore annEventStore) {
        this.eventStore = annEventStore;
    }



    @Override
    public void init(ProcessorContext<String, Event> context) {
        org.apache.kafka.streams.processor.api.Processor.super.init(context);
        this.context = context;
        this.kvStore = (KeyValueStore) context.getStateStore("aan-events-store");

        this.currentOffset = 0;
        this.currentHash = "dummy";
        this.aanModel = new AuroraOM();
        this.aanProjector = new KSAProjector("http://localhost:15002");
    }

    @Override
    public void process(Record<String, Command> commandRecord) {

        logger.info("### Procesando un nuevo comando {} {}", commandRecord.key(), commandRecord.toString());

        Command command = commandRecord.value();

        try {

            // OBTENER PROCESADOR DE COMANDO
            AANCommand commandProcessor = this.aanModel.getModelCommands().stream().filter(aanCommand -> aanCommand.commandName().equals(command.commandName())).findFirst().orElseThrow(() -> new CommandNotFoundException(command.commandName()));

            logger.info("Validando comando");

            Thread.sleep(2000);

            Queue<EventData> resultEvent = commandProcessor.applyCommand(command, this.aanProjector);

            logger.info("Comando validado");


//            Queue<EventData> resultEvent = commandProcessor.applyCommand(command);

            // ESTE VALOR DEBE VENIR DE LA VALIDACIÓN, YA QUE DEBE ESTAR CONSENSUADO PARA QUE PRODUZCA UN HASH DETERMINISTICO
            Instant validationTime = Instant.now();

            int eventNumber = 1;
            for (EventData eventData : resultEvent) {
                logger.info("Procesando evento {}/{}", eventNumber, resultEvent.size());

                String body = command.toString();

                String sha256hex = Hashing.sha256()
                        .hashString(body, StandardCharsets.UTF_8)
                        .toString();

                Event event = new Event(this.currentOffset.toString(), eventData.eventName(), eventData.eventData(), validationTime, sha256hex, command);

                this.aanDatabase.persistEvent(event).get();
                Record<String, Event> newRecord = new Record<>(event.eventId(), event, event.eventTimestamp().toEpochMilli()); // CREAR NUEVO EVENTO
                updateStore(event).get();

                context.forward(newRecord);
                this.currentOffset = this.currentOffset + 1;
                this.currentHash = sha256hex;
                eventNumber++;
            }

            logger.info("### Proceso finalizado");


        } catch (CommandNotFoundException e) {
            logger.info("### No se encontró procesador para comando {}", e.errorCode);
        } catch (CommandValidationException e) {
            logger.info("### Validación falló ({})", e.errorCode);
        } catch (CommandProjectorQueryException e) {
            logger.error("### Validación falló al utilizar el proyector");
        } catch (Exception e) {
            logger.error("### Validación falló (otro) {}", e);
        }

    }

    @Override
    public void close() {
        org.apache.kafka.streams.processor.api.Processor.super.close();
    }

    CompletableFuture<Void> updateStore(Event event) throws IOException {
        logger.info("Actualizando store {}", event);
        kvStore.put(event.eventId(), event);
        return this.eventStore.saveEvent(event);
    }


}
