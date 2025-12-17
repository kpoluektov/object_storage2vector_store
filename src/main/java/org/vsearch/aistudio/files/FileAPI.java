package org.vsearch.aistudio.files;

import com.openai.core.JsonValue;
import com.openai.core.MultipartField;
import com.openai.models.files.FileCreateParams;
import com.openai.models.files.FileObject;
import com.openai.models.files.FilePurpose;
import org.vsearch.Utils;
import org.vsearch.aistudio.AIStudioClient;
import org.vsearch.document.DocumentUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

public class FileAPI {
    private static final int hoursToExpire = Optional
            .ofNullable(Utils.getInt(Utils.AISTUDIO, "hoursToExpire"))
            .orElse(72 /*three days*/);
    public static FileObject upload(InputStream fileContent,
                                    String fileName, Map<String, JsonValue> attributes){
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        FileCreateParams params = FileCreateParams.builder()
                .purpose(FilePurpose.ASSISTANTS)
                .additionalBodyProperties(attributes)
                .expiresAfter(
                        FileCreateParams
                        .ExpiresAfter
                        .builder()
                        .seconds(hoursToExpire * 86400L)
                        .build()
                )
                .file(
                        MultipartField.<InputStream>builder()
                        .value(fileContent)
                        .filename(new String(fileName.getBytes(), StandardCharsets.ISO_8859_1))
                        .contentType(DocumentUtils.getMimeTypeByExtension(extension))
                        .build()
                )
                .build();
        return AIStudioClient.get().files().create(params);
    }

    public static FileObject retrieveFile(String fileId){
        return AIStudioClient.get().files().retrieve(fileId);
    }
}
