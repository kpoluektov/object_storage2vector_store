package org.vsearch;

import com.openai.core.JsonValue;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vsearch.aistudio.AIStudioClient;
import org.vsearch.db.DBConnection;
import org.vsearch.document.Document;
import org.vsearch.aistudio.vectorstore.VStore;
import org.vsearch.object_storage.S3NewTools;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

        if (args.length < 1){
            throw new Exception("Need path to properties yaml");
        }
        Utils.init(args[0]);
        S3NewTools.init();
        String bucket = Utils.getString(Utils.S3, "bucket");
        String extension = Utils.getString(Utils.S3, "extension");
        String VectorStoreId = Utils.getString(Utils.AISTUDIO, "indexId", true);
        AIStudioClient.init();
        VStore store;
        if (VectorStoreId != null) {
            store = new VStore(VectorStoreId);
        } else {
            store = new VStore();
        }
        ExecutorService executor = Executors.newFixedThreadPool(4);

        DBConnection connection = new DBConnection(Utils.getDBSettings());
        long startOverallTime = System.currentTimeMillis();
        int totalCount = 0;
        do{
            List<S3Object> objectList = S3NewTools.getPaginatedResponse(
                    bucket,
                    Utils.getString(Utils.S3, "prefix")
            );
            log.info("Found {} objects in bucket {}", objectList.size(), bucket);
            totalCount += objectList.size();
            long startTime = System.currentTimeMillis();
            if (! processChunk(objectList, extension, bucket, store, connection)){
                log.error("Chunk processing finished with errors. Exiting...");
                break;
            }
            log.info("{} objects finished in {} seconds. Total time is {} seconds",
                    totalCount,
                    (System.currentTimeMillis() - startTime)/1000,
                    (System.currentTimeMillis() - startOverallTime)/1000);
        }while (S3NewTools.hasMore());
        executor.shutdown();
        S3NewTools.close();
        log.info("{} objects total loaded in {} seconds", totalCount,
                (System.currentTimeMillis() - startOverallTime)/1000);
    }
    private static Runnable createDocumentTask(String key,
                                               String bucket,
                                               VStore store,
                                               DBConnection connection){
        return () -> {
            HashMap<String, JsonValue> attributes = new HashMap<>();
            attributes.put("uri", JsonValue.from(key));
            Document doc =
                    new Document(bucket, key, store
                            // attributes example
                            ,attributes
                    );
            connection.syncStatus(doc);
            try {
                log.debug("Working with object {}", key);
                if(!Document.Status.INITED.equals(doc.getStatus()) && Utils.getBoolean(Utils.AISTUDIO, "renew")){
                    doc.reinit();
                }
                doc.proceed();
                log.debug("Object with key {} added to index {}", key, store.getId());
            } catch (RuntimeException e) {
                Thread.currentThread().interrupt();
                log.error("Task: {} was interrupted with message {}", key, e.getMessage(), e);
            } finally {
                connection.saveRecord(doc);
            }
        };
    }
    private static Boolean processChunk(List<S3Object> objectList,
                                    String extension,
                                    String bucket,
                                    VStore store,
                                    DBConnection connection){
        List<Runnable> runnables = new ArrayList<>();
        for ( S3Object object : objectList ) {
            if (object.key().endsWith(extension)) {
                runnables.add(createDocumentTask(object.key(), bucket, store, connection));
            }
        }
        @NotNull List<CompletableFuture<Void>> completableFutures = runnables.stream()
                .map(CompletableFuture::runAsync)
                .collect(Collectors.toList());

        log.info("Start processing entities");
        try {
            CompletableFuture<Void> allOf = CompletableFuture
                    .allOf(completableFutures.toArray(CompletableFuture<?>[]::new));
            allOf.join();
        } catch (CompletionException ex) {
            // Handle exceptions from any of the CompletableFutures
            log.error("Global exception occurred: ", ex);
        }
        return completableFutures.stream().allMatch(CompletableFuture::isDone);
    }
}