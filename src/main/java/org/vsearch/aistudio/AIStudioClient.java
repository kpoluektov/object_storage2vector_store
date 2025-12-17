package org.vsearch.aistudio;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vsearch.Utils;

public class AIStudioClient {
    private static OpenAIClient client;
    public static void init(String apiKey, String baseUrl, String project){
        client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .project(project)
                .build();
    }
    public static void init(){
        init(
                Utils.getString(Utils.AISTUDIO, "apiKey"),
                Utils.getString(Utils.AISTUDIO, "baseUrl"),
                Utils.getString(Utils.AISTUDIO, "project")
        );
    }
    public static OpenAIClient get(){
        return client;
    }
}