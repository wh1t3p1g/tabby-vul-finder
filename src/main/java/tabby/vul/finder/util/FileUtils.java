package tabby.vul.finder.util;

import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import tabby.vul.finder.config.GlobalConfiguration;
import tabby.vul.finder.data.Cypher;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * @author wh1t3P1g
 * @since 2020/10/22
 */
@Slf4j
public class FileUtils {

    public static <T> T getJsonContent(String path, Class<T> type){
        File file = new File(path);
        if(!file.exists()) return null;
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            return GlobalConfiguration.GSON.fromJson(reader, type);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Set<Cypher> getYamlContent(String path){
        File file = new File(path);
        if(!file.exists()) return null;
        try {
            Set<Cypher> cyphers = new HashSet<>();
            Yaml yaml = new Yaml(new Constructor(Cypher.class));

            Reader reader = Files.newBufferedReader(Paths.get(path));
            for(Object cypher:yaml.loadAll(reader)){
                cyphers.add((Cypher) cypher);
            }
            return cyphers;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new HashSet<>();
    }

    public static String getFileContent(Path path){
        try {
            byte[] content = Files.readAllBytes(path);
            return new String(content);
        } catch (IOException e) {
            return null;
        }
    }

    public static boolean fileExists(String path){
        if(path == null) return false;

        File file = new File(path);
        return file.exists();
    }

    public static boolean fileNotExists(String path){
        if(path == null) return true;

        File file = new File(path);
        return !file.exists();
    }

    public static void fileNotExistsWithException(String path) throws FileNotFoundException {
        if(path != null){
            File file = new File(path);
            if(file.exists()){
                return;
            }
        }
        throw new FileNotFoundException(path + " not found.");
    }

    public static void delete(String filepath){
        File file = new File(filepath);
        if (file.exists()) {
            file.delete();
        }
    }

    public static String getWinPath(String path){
        if(JavaVersion.isWin()){
            path = "/"+path.replace("\\", "/");
        }
        return path;
    }

    public static void createDirectory(String path){
        File file = new File(path);
        if(file.mkdirs()){
            log.info("Create directory {} success!", path);
        }
    }

    public static String getRealPath(String filepath) throws IllegalArgumentException{
        try{
            Path path = Paths.get(filepath);
            return path.toRealPath().toString();
        }catch (Exception ig){
            throw new IllegalArgumentException("Path "+ filepath +" error!");
        }
    }

    public static String getLastPath(String filepath) throws IllegalArgumentException{
        try{
            Path path = Paths.get(filepath);
            return path.getFileName().toString();
        }catch (Exception ig){
            throw new IllegalArgumentException("Cache Path error!");
        }
    }
}
