package org.vsearch.doc;

public class DocumentUtils {
    public static String getMimeTypeByExtension(String extension){
        switch (extension.toLowerCase()){
            case "txt" :
                return "text/plain";
            case "json":
                return "application/json";
            case "md":
                return "text/markdown";
            case "pdf":
                return "application/pdf";
            default:
                throw new RuntimeException("Cannot identify mime-type by extension " + extension);
        }
    }
}
