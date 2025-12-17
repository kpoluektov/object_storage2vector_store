package org.vsearch.document;
import com.openai.core.JsonValue;
import com.openai.models.files.FileObject;

import com.openai.models.vectorstores.files.VectorStoreFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vsearch.object_storage.S3NewTools;

import org.vsearch.Utils;
import org.vsearch.aistudio.files.FileAPI;
import org.vsearch.aistudio.vectorstore.VStore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Document {
    private static final Logger log = LoggerFactory.getLogger(Document.class);
    private static final boolean skipStatusVerify = Optional
            .ofNullable(Utils.getBoolean(Utils.AISTUDIO, "skipStatusVerify"))
            .orElse(false);
    public enum Status {
        INITED,
        LOADED,
        UPLOADED,
        ADDED,
        FINISHED,
        ERROR
    }
    private Map<String, JsonValue> attributes;
    private final String key;
    private final String bucket;
    private Status status;
    private FileObject fileObject;
    private final VStore store;

    public Document(String bucket, String key, VStore store){
        this.bucket = bucket;
        this.key = key;
        this.store = store;
        this.attributes = new HashMap<>();
        this.status = Status.INITED;
    }
    public Document(String bucket, String key, VStore store, Map<String, JsonValue> attributes){
        this(bucket, key, store);
        this.attributes = attributes;
    }
    private void load() {
        // body is redundant
        this.status = Status.LOADED;
    }
    public void proceed() {
        while(!(Status.FINISHED.equals(this.status) || (
        skipStatusVerify && Status.ADDED.equals(this.status)))){
            try {
                next();
            } catch (IOException | InterruptedException e) {
                log.error("Error with key {} at status {}", this.key, this.status);
                throw new RuntimeException(e);
            }
        }
    }
    private void next() throws IOException, InterruptedException {
        switch (this.status) {
            case INITED:
                load();
                break;
            case LOADED:
                upload();
                break;
            case UPLOADED:
                addFileToIndex(store);
                break;
            case ADDED:
                finishIt();
                break;
            case FINISHED:
                break;
            default:
                throw new RuntimeException("Unknown status " + this.status);
        }
    }

    private void upload(){
        String fileName = this.key.substring(this.key.lastIndexOf('/') + 1);
        try {
            this.fileObject =
                    FileAPI.upload(S3NewTools.getObjectContent(bucket, key), fileName, attributes);
        } catch (Exception e) {
            log.error("Can't write object {}", this.key);
            throw e;
//        } finally {
//            this.body.getObjectContent().close();
        }
        log.debug("File {} loaded", fileName);
        this.status = Status.UPLOADED;
    }

    private void finishIt() throws InterruptedException {
        if (Status.FINISHED.equals(this.status)){
            //all is done
            return;
        }
        //if not verify - leave it in added status
        if (skipStatusVerify) return;

        finishIt(store.retrieveStatus(this.fileObject.id()));
    }
    private void finishIt(VectorStoreFile.Status status){
        if (Status.ADDED.equals(this.status)
                && "completed".equals(status.asString())) {
            this.status = Status.FINISHED;
        }
    }
    private void addFileToIndex(VStore store) {
        VectorStoreFile file = store.addFileToIndex(this.fileObject, this.attributes);
        this.status = Status.ADDED;
        finishIt(file.status());
    }

    public Status getStatus() {
        return status;
    }
    public String getIndex(){
        return store.getId();
    }
    public String getKey(){
        return key;
    }
    public String getFileId(){
        return this.fileObject.id();
    }
    public void setStatus(String status){
        this.status = Status.valueOf(status);
    }
    public void setFileObject(String fileId){
        this.fileObject= FileAPI.retrieveFile(fileId);
    }
}
