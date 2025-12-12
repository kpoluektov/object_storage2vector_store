package org.vsearch;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AIStudioClient {
    private static final Log log = LogFactory.getLog(AIStudioClient.class);
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
                Utils.getString("aistudio", "apiKey"),
                Utils.getString("aistudio", "baseUrl"),
                Utils.getString("aistudio", "project")
        );
    }
    public static OpenAIClient get(){
        return client;
    }
}