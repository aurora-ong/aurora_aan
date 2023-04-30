package ong.aurora.aan.core.command_pool;

import com.google.common.net.HostAndPort;
import io.javalin.Javalin;
import io.javalin.http.Context;
import ong.aurora.aan.command.Command;
import ong.aurora.aan.core.AANProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

public class CommandRestService {

    HostAndPort hostAndPort;

    AANProcessor aanProcessor;

    CommandPool commandPool;
    private static final Logger log = LoggerFactory.getLogger(CommandRestService.class);

    public CommandRestService(HostAndPort hostAndPort, AANProcessor aanProcessor, CommandPool commandPool) {
        this.hostAndPort = hostAndPort;
        this.aanProcessor = aanProcessor;
        this.commandPool = commandPool;
    }

    public void start() {
        Javalin app = Javalin.create().start(hostAndPort.getHost(), hostAndPort.getPort());

        app.post("/command/new", this::newCommandHandler);

        log.info("CommandRestService iniciado en {}:{}", hostAndPort.getHost(), hostAndPort.getPort());

    }

    void newCommandHandler(Context context) {

        try {
            CreateCommandData commandData = context.bodyAsClass(CreateCommandData.class);
            log.info("Context {}", context.bodyAsClass(CreateCommandData.class));
            Command command = new Command(UUID.randomUUID().toString(), Instant.now(), commandData.commandName(), commandData.commandData());
            log.info("Nuevo comando {}", command);
            new Thread(() -> this.aanProcessor.process(command).join()).start();
            commandPool.addCommand(command);
            context.status(200).result(command.commandId());
        } catch (Exception e) {
            context.status(400).result("Error");
        }


    }

}
