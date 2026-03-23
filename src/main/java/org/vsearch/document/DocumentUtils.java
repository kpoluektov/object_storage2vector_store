package org.vsearch.document;

public class DocumentUtils {
    public static String getMimeTypeByExtension(String extension){
        switch (extension.toLowerCase()){
            case "wiki":
            case "txt":
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
