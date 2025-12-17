package org.vsearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vsearch.aistudio.AIStudioClient;
import org.vsearch.db.DBConnection;
import org.vsearch.document.Document;
import org.vsearch.aistudio.vectorstore.VStore;
import org.vsearch.object_storage.S3NewTools;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

        if (args.length < 1){
            throw new Exception("Need path to properties yaml");
        }
        Utils.init(args[0]);
        S3NewTools.init();
        String bucket = Utils.getString("s3", "bucket");
        String extension = Utils.getString("s3", "extension");
        String VectorStoreId = Utils.getString("aistudio", "indexId", true);
        AIStudioClient.init();
        VStore store;
        if (VectorStoreId != null) {
            store = new VStore(VectorStoreId);
        } else {
            store = new VStore();
        }
        ExecutorService executor = Executors.newFixedThreadPool(4);
        DBConnection connection = new DBConnection(Utils.getDBSettings());
        int totalCount = 0;
        do{
            List<S3Object> objectList = S3NewTools.getPaginatedResponse(
                    bucket,
                    Utils.getString("s3", "prefix")
            );
            log.info("Found {} objects in bucket {}", objectList.size(), bucket);
            totalCount += objectList.size();
            List<Runnable> runnables = new ArrayList<>();
            for ( S3Object object : objectList ) {
                if (object.key().endsWith(extension)) {
                    runnables.add(createDocumentTask(object.key(), bucket, store, connection));
                }
            }
            CompletableFuture<?>[] completableFutures = runnables.stream()
                    .map(CompletableFuture::runAsync)
                    .toArray(CompletableFuture<?>[]::new);
            log.info("Start processing entities");
            long startTime = System.currentTimeMillis();
            try {
                CompletableFuture.allOf(completableFutures).join();
            } catch (CompletionException ex) {
                // Handle exceptions from any of the CompletableFutures
                log.error("Global exception occurred: ", ex);
            }
            log.info("{} objects finished in {} seconds", totalCount, (System.currentTimeMillis() - startTime)/1000);
        }while (S3NewTools.hasMore());
        executor.shutdown();
        S3NewTools.close();
    }
    private static Runnable createDocumentTask(String key, String bucket, VStore store, DBConnection connection){
        return () -> {
            Document doc =
                    new Document(bucket, key, store
                    // attributes example       , Map.of("uri", JsonValue.from(key))
                    );
            connection.syncStatus(doc);
            try {
                log.debug("Working with object {}", key);
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
}