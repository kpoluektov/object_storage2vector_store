package org.vsearch.vectorstore;

import com.openai.models.vectorstores.VectorStore;
import com.openai.models.vectorstores.VectorStoreCreateParams;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vsearch.AIStudioClient;
import org.vsearch.Utils;

public class VStore {
    private static final Logger log = LoggerFactory.getLogger(VStore.class);
    @NotNull
    VectorStore store;
    public VStore(String index){
        this.store = AIStudioClient.get().vectorStores().retrieve(index);
        log.info("Vector store with id {} retrieved", store.id());
    }
    public VStore(){
        VectorStoreCreateParams params = VectorStoreCreateParams.builder()
                .name(Utils.getString("aistudio", "indexName"))
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
}
