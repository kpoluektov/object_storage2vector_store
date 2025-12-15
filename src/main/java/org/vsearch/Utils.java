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

    private static Map<String, Object> getFamily(String family){
        if (settings.containsKey(family)){
            return (Map<String, Object>) settings.get(family);
        } else throw new RuntimeException("Not found key " + family);
    }
    public static String getString(String family, String path){
        return (String) getFamily(family).get(path);
    }
    public static int getInt(String family, String path){
        return (int) getFamily(family).get(path);
    }
    public static Map<String, Object> getDBSettings(){
        return getFamily("pg");
    }
}
