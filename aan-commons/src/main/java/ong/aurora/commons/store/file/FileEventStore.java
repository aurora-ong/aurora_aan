package ong.aurora.commons.store.file;

import ong.aurora.commons.event.Event;
import ong.aurora.commons.serialization.ANNSerializer;
import ong.aurora.commons.serialization.jackson.ANNJacksonSerializer;
import ong.aurora.commons.store.ANNEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class FileEventStore implements ANNEventStore {

    private static final Logger log = LoggerFactory.getLogger(FileEventStore.class);

    File file;
    FileWriter fileWriter;

    PrintWriter printWriter;

    BufferedReader bufferedReader;


    public FileEventStore(String path) throws IOException {
        this.file = new File(path);
        if (file.createNewFile()) {
            System.out.println("File created: " + file.getName());
        } else {
            System.out.println("File already exists.");
        }
        boolean setWrittable = file.setWritable(true, false);
        log.info("setWrittable {}", setWrittable);
        fileWriter = new FileWriter(file, StandardCharsets.UTF_8, true);
        BufferedWriter bw = new BufferedWriter(fileWriter);
        FileReader fileReader = new FileReader(file, StandardCharsets.UTF_8);
        this.bufferedReader = new BufferedReader(fileReader);


        this.printWriter = new PrintWriter(bw);
    }

    @Override
    public CompletableFuture<Void> saveEvent(String event) throws Exception {
        log.info("Escribiendo evento en {}", this.file.getCanonicalPath());
        printWriter.println(event);
        printWriter.flush();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public Stream<String> readEventStore() {
        try {
            return Files.lines(this.file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
