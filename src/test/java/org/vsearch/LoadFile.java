package org.vsearch;

import org.junit.jupiter.api.Test;
import org.vsearch.doc.Document;

public class LoadFile {
    @Test
    public void TestOneFile() {
        Utils.init("src/test/resources/application.yaml");
        S3NewTools.init();
        Document doc = new Document(
                "pol-bucket-for-agent",
                "input/file1.txt",
                "fvtpomehaovhjoqv3cfk");
        AIStudioClient.init();
        doc.proceed();
    }
}
