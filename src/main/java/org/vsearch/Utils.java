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

    private static Map<String, Object> getFamily(String family){
        if (settings.containsKey(family)){
            return (Map<String, Object>) settings.get(family);
        } else throw new RuntimeException("Not found family key " + family);
    }
    public static Object getObject(String family, String path, boolean doNotThrowEx){
        var familyGroup = getFamily(family);
        if (familyGroup.containsKey(path))
            return familyGroup.get(path);
        else if (!doNotThrowEx) throw new RuntimeException("Not found key " + path + " for family " + family);
        return null;
    }
    public static String getString(String family, String path){
        return getString(family, path, false);
    }
    public static String getString(String family, String path, boolean doNotThrowEx){
        return (String) getObject(family, path, doNotThrowEx);
    }
    public static Integer getInt(String family, String path){
        return (Integer) getObject(family, path, true);
    }
    public static Boolean getBoolean(String family, String path){
        return (Boolean) getObject(family, path, true);
    }
    public static Map<String, Object> getDBSettings(){
        return getFamily("pg");
    }
}
