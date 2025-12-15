package org.vsearch.doc;

import com.amazonaws.services.s3.model.S3Object;
import com.openai.core.MultipartField;
import com.openai.models.files.FileCreateParams;
import com.openai.models.files.FileObject;
import com.openai.models.files.FilePurpose;

import com.openai.models.vectorstores.files.VectorStoreFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vsearch.AIStudioClient;
import org.vsearch.S3Tools;

import javax.mail.internet.MimeUtility;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Document {
    private static final Logger log = LoggerFactory.getLogger(Document.class);

    public enum Status {
        INITED,
        LOADED,
        UPLOADED,
        ADDED,
        FINISHED,
        ERROR
    }

    private S3Object body;
    private final String key;
    private final String bucket;
    private Status status;
    private FileObject fileObject;
    private final String index;

    public Document(String bucket, String key, String index){
        this.bucket = bucket;
        this.key = key;
        this.index = index;
        this.status = Status.INITED;
    }
    public Document(String bucket, String key, String index, Map<String, String> attributes){
        this(bucket, key, index);
    }
    private void load() {
        this.body = S3Tools.getObject(bucket, key);
        this.status = Status.LOADED;
    }
    public void proceed() {
        while(!Status.FINISHED.equals(this.status)){
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
                addFileToIndex();
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

    private void upload() throws IOException {
        String fileName = this.body.getKey().substring(this.body.getKey().lastIndexOf('/') + 1);
        String extension = null;
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i+1);
        }

        com.openai.models.files.FileCreateParams params = FileCreateParams.builder()
                .purpose(FilePurpose.FINE_TUNE)
                .expiresAfter(FileCreateParams.ExpiresAfter.builder().seconds(10000).build())
                .file(MultipartField.<InputStream>builder()
                        .value(this.body.getObjectContent())
                        .filename(new String(fileName.getBytes(), StandardCharsets.ISO_8859_1))
                        .contentType(DocumentUtils.getMimeTypeByExtension(extension))
                        .build())
                .build();
        try {
            this.fileObject = AIStudioClient.get().files().create(params);
        } catch (Exception e) {
            log.error("Can't write object {}", this.body.getKey());
            throw e;
        } finally {
            this.body.getObjectContent().close();
        }
        log.debug("File {} loaded", fileName);
        this.status = Status.UPLOADED;
    }
    private com.openai.models.vectorstores.files.FileCreateParams createParams(){
        return com.openai.models.vectorstores.files.FileCreateParams.builder()
                        .fileId(this.fileObject.id())
                        .vectorStoreId(this.index)
                        .build();
    }
    private void finishIt() throws InterruptedException {
        if (Status.FINISHED.equals(this.status)){
            //all is done
            return;
        }
        com.openai.models.vectorstores.files.FileRetrieveParams params
                = com.openai.models.vectorstores.files.FileRetrieveParams.builder()
                .fileId(this.fileObject.id())
                .vectorStoreId(this.index)
                .build();
        VectorStoreFile.Status curStatus = retrieveStatus(params);
        finishIt(curStatus);
    }
    private void finishIt(VectorStoreFile.Status status){
        if (Status.ADDED.equals(this.status)
                && "completed".equals(status.asString())) {
            this.status = Status.FINISHED;
        }
    }
    private void addFileToIndex() {
        VectorStoreFile file = AIStudioClient.get()
                .vectorStores()
                .files()
                .create(this.index, createParams());
        this.status = Status.ADDED;
        finishIt(file.status());
    }
    private VectorStoreFile.Status retrieveStatus(com.openai.models.vectorstores.files.FileRetrieveParams params) throws InterruptedException {
        int cnt = 3;
        VectorStoreFile file;
        while (cnt > 0){
            try{
                file = AIStudioClient.get().vectorStores().files().retrieve(params);
                return file.status();
            } catch (com.openai.errors.NotFoundException e) {
                TimeUnit.MILLISECONDS.sleep(500);
                cnt--;
            }
        }
        throw new RuntimeException("Cannot get status for file " + params.fileId());
    }

    public String getStatus() {
        return status.name();
    }
    public String getFileId(){
        return fileObject.id();
    }
    public String getIndex(){
        return index;
    }
    public String getKey(){
        return key;
    }
    public void setStatus(String status){
        this.status = Status.valueOf(status);
    }

    public void setFileObject(String fileId){
        this.fileObject= AIStudioClient.get().files().retrieve(fileId);
    }
}
