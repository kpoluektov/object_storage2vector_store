package org.vsearch;

import org.junit.jupiter.api.Test;
import org.vsearch.aistudio.AIStudioClient;
import org.vsearch.aistudio.vectorstore.VStore;
import org.vsearch.document.Document;
import org.vsearch.object_storage.S3NewTools;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LoadFileTest {
    @Test
    public void TestOneFile() {
        Utils.init("src/test/resources/application.yaml");
        S3NewTools.init();
        AIStudioClient.init();
        VStore store = new VStore();
        assertNotNull(store.getId());
        Document doc = new Document(
                "pol-bucket-for-agent",
                "input/file1.txt",
                store);
        doc.proceed();
        List<Document.Status> statuses = Arrays.asList(Document.Status.ADDED, Document.Status.FINISHED);
        assertTrue(statuses.contains(doc.getStatus()));
        S3NewTools.close();
    }
}
