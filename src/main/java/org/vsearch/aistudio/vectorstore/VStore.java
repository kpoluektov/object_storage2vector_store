package org.vsearch.aistudio.vectorstore;

import com.openai.core.JsonValue;
import com.openai.models.files.FileObject;
import com.openai.models.vectorstores.VectorStore;
import com.openai.models.vectorstores.VectorStoreCreateParams;
import com.openai.models.vectorstores.files.FileCreateParams;
import com.openai.models.vectorstores.files.FileRetrieveParams;
import com.openai.models.vectorstores.files.VectorStoreFile;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vsearch.aistudio.AIStudioClient;
import org.vsearch.Utils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class VStore {

    private static final Logger log = LoggerFactory.getLogger(VStore.class);
    @NotNull
    VectorStore store;

    private static final int retryCount = Optional
            .ofNullable(Utils.getInt(Utils.AISTUDIO, "retryCount"))
            .orElse(3);
    private static final int waitMillis = Optional
            .ofNullable(Utils.getInt(Utils.AISTUDIO, "waitMillis"))
            .orElse(100);

    public VStore(String index){
        this.store = AIStudioClient.get().vectorStores().retrieve(index);
        log.info("Vector store with id {} retrieved", store.id());
    }
    public VStore(){
        String indexName = Utils.getString(Utils.AISTUDIO, "indexName");
        VectorStoreCreateParams params = VectorStoreCreateParams.builder()
                .name(indexName)
                .build();
        store = AIStudioClient.get().vectorStores().create(params);
        log.info("New Vector store with id {} added", store.id());
    }
    public VStore(@NotNull VectorStore store){
        this.store = store;
    }
    public VectorStore.Status getStatus(){
        return store.status();
    }
    public String getId(){
        return store.id();
    }
    public VectorStoreFile addFileToIndex(FileObject file, Map<String, JsonValue> attributes) {
        return AIStudioClient.get()
                .vectorStores()
                .files()
                .create(this.getId() , getCreateParams(file, attributes));
    }
    private FileCreateParams getCreateParams(FileObject file, Map<String, JsonValue> attributes){
        FileCreateParams.Attributes fAttributes = FileCreateParams.Attributes.builder()
                .additionalProperties(attributes)
                .build();
        return FileCreateParams.builder()
                .fileId(file.id())
                .additionalBodyProperties(attributes)
                .attributes(fAttributes)
                .vectorStoreId(this.getId())
                .build();
    }
    private VectorStoreFile retrieveFile(String file){
        FileRetrieveParams params =  FileRetrieveParams.builder()
                .fileId(file)
                .vectorStoreId(this.getId())
                .build();
        return AIStudioClient.get().vectorStores().files().retrieve(params);
    }

    public VectorStoreFile.Status retrieveStatus(String fileId) throws InterruptedException {
        int cnt = retryCount;
        VectorStoreFile file;
        while (cnt > 0){
            try{
                file = retrieveFile(fileId);
                return file.status();
            } catch (com.openai.errors.NotFoundException e) {
                TimeUnit.MILLISECONDS.sleep(waitMillis);
                cnt--;
            }
        }
        throw new RuntimeException("Cannot get status for file " + fileId);
    }
}
