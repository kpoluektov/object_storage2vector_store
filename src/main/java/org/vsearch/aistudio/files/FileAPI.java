package org.vsearch.aistudio.files;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.core.JsonValue;
import com.openai.core.MultipartField;
import com.openai.models.files.FileCreateParams;
import com.openai.models.files.FileObject;
import com.openai.models.files.FilePurpose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vsearch.Utils;
import org.vsearch.aistudio.AIStudioClient;
import org.vsearch.document.DocumentUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

public class FileAPI {
    private static final Logger logger = LoggerFactory.getLogger(FileAPI.class);
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
                .putAdditionalBodyProperty("attributes", JsonValue.from(convertAttributesToString(attributes)))
                .expiresAfter(
                        FileCreateParams
                        .ExpiresAfter
                        .builder()
                        .seconds(hoursToExpire * 3600L)
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
    // method to dump attributes as Json
    private static String convertAttributesToString(Map<String, JsonValue> attributes) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // Convert map to String, handles proper quoting of values
            return objectMapper.writeValueAsString(attributes);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "{}";
        }
    }
}
