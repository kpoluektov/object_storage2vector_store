package org.vsearch;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vsearch.doc.Document;
import org.vsearch.vectorstore.VStore;

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
        S3Tools.init();
        String bucket = Utils.getString("s3", "bucket");
        String extension = Utils.getString("s3", "extension");
        String VectorStoreId = Utils.getString("aistudio", "indexId");
        AIStudioClient.init();
        VStore store;
        if (VectorStoreId != null) {
            store = new VStore(VectorStoreId);
        } else {
            store = new VStore();
        }
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<S3ObjectSummary> objectList = S3Tools.getObjects(
                    bucket,
                    Utils.getString("s3", "prefix")
                ).getObjectSummaries();
        log.info("Found {} objects", objectList.size());
        List<Runnable> runnables = new ArrayList<>();
        for ( S3ObjectSummary summary : objectList ) {
            if (summary.getKey().endsWith(extension)) {
                runnables.add(createDocumentTask(summary.getKey(), bucket, store));
            }
        }
        CompletableFuture<?>[] completableFutures = runnables.stream()
                .map(CompletableFuture::runAsync)
                .toArray(CompletableFuture<?>[]::new);
        try {
            CompletableFuture.allOf(completableFutures).join();
        } catch (CompletionException ex) {
            // Handle exceptions from any of the CompletableFutures
            log.error("An exception occurred: {}", ex.getCause().getMessage());
        }
        executor.shutdown();
    }
    private static Runnable createDocumentTask(String key, String bucket, VStore store){
        return new Runnable() {
            @Override
            public void run() {
                try {
                    log.info("Working with object {}", key);
                    Document doc = new Document(bucket, key, store.getId());
                    doc.proceed();
                    log.info("Object with key {} added to index {}", key, store.getId());
                } catch (RuntimeException e) {
                    Thread.currentThread().interrupt();
                    log.error("Task: {} was interrupted with message {}", key, e.getMessage(), e);
                }
            }
        };
    }
}