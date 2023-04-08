package ong.aurora.ann;

import ong.aurora.commons.blockchain.AANBlockchain;
import ong.aurora.commons.command.*;
import ong.aurora.commons.event.Event;
import ong.aurora.commons.event.EventData;
import ong.aurora.commons.model.AANModel;
import ong.aurora.commons.projector.AANProjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

public class AANProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AANProcessor.class);

    AANModel aanModel;
    AANBlockchain aanBlockchain;
    AANProjector aanProjector;

    public AANProcessor(AANBlockchain aanBlockchain, AANModel aanModel, AANProjector aanProjector) {
        this.aanBlockchain = aanBlockchain;
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

                logger.info("Siguiente hash: {}",aanBlockchain.nextBlockHash());

                Event event = new Event(this.aanBlockchain.blockCount(), eventData.eventName(), eventData.eventData(), validationTime, aanBlockchain.nextBlockHash(), command);

                this.aanBlockchain.persistEvent(event);

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

}
