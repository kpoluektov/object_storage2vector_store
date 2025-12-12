package org.vsearch;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

public class Utils {
    protected static Map<String, Object> settings;
    public static void init(String file) {

        try{
            InputStream inputStream = new FileInputStream(file);
            Yaml yaml = new Yaml();
            settings = yaml.load(inputStream);
        } catch (FileNotFoundException e) {
            throw  new RuntimeException("Not found " + file);
        }

    }
    private static Object get(String path){
        if (settings.containsKey(path)){
            return settings.get(path);
        } else throw new RuntimeException("Not found key " + path);
    }

    private static Map<String, String> getFamily(String family){
        if (settings.containsKey(family)){
            return (Map<String, String>) settings.get(family);
        } else throw new RuntimeException("Not found key " + family);
    }
    public static String getString(String family, String path){
        return getFamily(family).get(path);
    }
    public static Map<?,?> getMap(String path){
        return (Map<?,?>) settings.get(path);
    }
    public static Integer getInteger(String path){
        return (Integer) get(path);
    }
    public static Boolean getBoolean(String path){
        return (Boolean) get(path);
    }
}
