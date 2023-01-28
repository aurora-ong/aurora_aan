package ong.aurora.ann;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import ong.aurora.commons.blockchain.ANNBlockchain;
import ong.aurora.commons.command.*;
import ong.aurora.commons.event.Event;
import ong.aurora.commons.event.EventData;
import ong.aurora.commons.model.AANModel;
import ong.aurora.commons.projector.AANProjector;
import ong.aurora.model.v_0_0_1.AuroraOM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

public class AANProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AANProcessor.class);

    private String currentHash;

    private final AANProjector aanProjector;

    AANModel aanModel;
    ANNBlockchain annBlockchain;

    public AANProcessor(ANNBlockchain annBlockchain, AANProjector aanProjector, AANModel aanModel) {
        this.annBlockchain = annBlockchain;
        this.currentHash = "dummy";
        this.aanModel = aanModel;
        this.aanProjector = aanProjector;
    }

    public CompletableFuture<Void> process(Command command) {

        logger.info("### Procesando un nuevo comando {}", command.toString());

        try {

            // OBTENER PROCESADOR DE COMANDO
            AANCommand commandProcessor = this.aanModel.getModelCommands().stream().filter(aanCommand -> aanCommand.commandName().equals(command.commandName())).findFirst().orElseThrow(() -> new CommandNotFoundException(command.commandName()));

            logger.info("Validando comando");

            Thread.sleep(2000);

            Queue<EventData> resultEvent = commandProcessor.applyCommand(command, this.aanProjector);

            logger.info("Comando validado");

            // ESTE VALOR DEBE VENIR DE LA VALIDACIÓN, YA QUE DEBE ESTAR CONSENSUADO PARA QUE PRODUZCA UN HASH DETERMINISTICO
            Instant validationTime = Instant.now();

            int eventNumber = 1;
            for (EventData eventData : resultEvent) {
                logger.info("Procesando evento {}/{}", eventNumber, resultEvent.size());

                logger.info("Último evento: {}", annBlockchain.lastEventHash().orElse(null));

                Event event = new Event(this.annBlockchain.blockCount(), eventData.eventName(), eventData.eventData(), validationTime, annBlockchain.lastEventHash().orElse(null), command);

                updateEventStore(event).get();
                aanProjector.projectEvent(event).get();

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

        return CompletableFuture.completedFuture(null);

    }

    CompletableFuture<Void> updateEventStore(Event event) throws Exception {
        logger.info("Actualizando store {}", event);
        return this.annBlockchain.persistEvent(event);
    }


}
