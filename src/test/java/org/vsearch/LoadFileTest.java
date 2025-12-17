package org.vsearch;

import org.junit.jupiter.api.Test;
import org.vsearch.aistudio.AIStudioClient;
import org.vsearch.aistudio.vectorstore.VStore;
import org.vsearch.document.Document;
import org.vsearch.object_storage.S3NewTools;

public class LoadFileTest {
    @Test
    public void TestOneFile() {
        String index = "fvtpomehaovhjoqv3cfk";
        Utils.init("src/test/resources/application.yaml");
        S3NewTools.init();
        Document doc = new Document(
                "pol-bucket-for-agent",
                "input/file1.txt",
                new VStore(index));
        AIStudioClient.init();
        doc.proceed();
    }
}
