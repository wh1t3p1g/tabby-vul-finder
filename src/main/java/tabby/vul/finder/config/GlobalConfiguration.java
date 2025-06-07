package tabby.vul.finder.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import tabby.vul.finder.util.FileUtils;

import java.io.*;
import java.util.Properties;

/**
 * @author wh1t3P1g
 * @since 2020/11/9
 */
@Slf4j
public class GlobalConfiguration {

    public static String CONFIG_FILE_PATH;
    public static String CLASSES_CACHE_PATH;
    public static String METHODS_CACHE_PATH;
    public static String CALL_RELATIONSHIP_CACHE_PATH;
    public static String ALIAS_RELATIONSHIP_CACHE_PATH;
    public static String EXTEND_RELATIONSHIP_CACHE_PATH;
    public static String HAS_RELATIONSHIP_CACHE_PATH;
    public static String INTERFACE_RELATIONSHIP_CACHE_PATH;

    public static String OUTPUT_DIRECTORY = "";
    public static String CYPHER_RESULT_DIRECTORY_PREFIX = "result";
    public static String CYPHER_RESULT_DIRECTORY = String.join(File.separator, System.getProperty("user.dir"), "result");
    public static String CYPHER_RULE_PATH = null;
    public static String NEO4J_USERNAME = null;
    public static String NEO4J_PASSWORD = null;
    public static String NEO4J_URL = null;
    public static boolean IS_DOCKER_IMPORT_PATH = false;
    public static String CSV_PATH = "";


    public static Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    public static void init() {
        if (CONFIG_FILE_PATH == null) {
            CONFIG_FILE_PATH = String.join(File.separator, System.getProperty("user.dir"), "config", "db.properties");
            if(FileUtils.fileNotExists(CONFIG_FILE_PATH)) {
                String tabbyHome = System.getenv("TABBY_HOME");
                if(tabbyHome != null) {
                    CONFIG_FILE_PATH = String.join(File.separator, tabbyHome, "config", "db.properties");
                }
            }
        }else{
            if(FileUtils.fileNotExists(CONFIG_FILE_PATH)) {
                log.error(CONFIG_FILE_PATH+" not found!");
                System.exit(0);
            }
        }

    }

    public static void initConfig(boolean isLoad, boolean isFind) throws FileNotFoundException {
        Properties props = new Properties();
        // read from config/settings.properties
        try(Reader reader = new FileReader(CONFIG_FILE_PATH)){
            props.load(reader);
        } catch (IOException e) {
            throw new FileNotFoundException(CONFIG_FILE_PATH + " not found.");
        }
        // apply to GlobalConfiguration
        NEO4J_USERNAME = getProperty("tabby.neo4j.username", "neo4j", props);
        NEO4J_PASSWORD = getProperty("tabby.neo4j.password", "neo4j", props);
        NEO4J_URL = getProperty("tabby.neo4j.url", "bolt://localhost:7687", props);

        IS_DOCKER_IMPORT_PATH = getBooleanProperty("tabby.cache.isDockerImportPath", "false", props);

        if(IS_DOCKER_IMPORT_PATH){
            OUTPUT_DIRECTORY = String.join(File.separator, "/var/lib/neo4j/import", CSV_PATH);
        }else{
            OUTPUT_DIRECTORY = CSV_PATH;
        }

        if(!IS_DOCKER_IMPORT_PATH){
            if(!FileUtils.fileExists(OUTPUT_DIRECTORY)){
                FileUtils.createDirectory(OUTPUT_DIRECTORY);
            }
            OUTPUT_DIRECTORY = FileUtils.getRealPath(OUTPUT_DIRECTORY);
        }
        // resolve cache directory
        CLASSES_CACHE_PATH = String.join(File.separator,OUTPUT_DIRECTORY, "GRAPHDB_PUBLIC_CLASSES.csv");
        METHODS_CACHE_PATH = String.join(File.separator,OUTPUT_DIRECTORY, "GRAPHDB_PUBLIC_METHODS.csv");
        CALL_RELATIONSHIP_CACHE_PATH = String.join(File.separator,OUTPUT_DIRECTORY, "GRAPHDB_PUBLIC_CALL.csv");
        ALIAS_RELATIONSHIP_CACHE_PATH = String.join(File.separator,OUTPUT_DIRECTORY, "GRAPHDB_PUBLIC_ALIAS.csv");
        EXTEND_RELATIONSHIP_CACHE_PATH = String.join(File.separator,OUTPUT_DIRECTORY, "GRAPHDB_PUBLIC_EXTEND.csv");
        HAS_RELATIONSHIP_CACHE_PATH = String.join(File.separator,OUTPUT_DIRECTORY, "GRAPHDB_PUBLIC_HAS.csv");
        INTERFACE_RELATIONSHIP_CACHE_PATH = String.join(File.separator,OUTPUT_DIRECTORY, "GRAPHDB_PUBLIC_INTERFACES.csv");

        if(isFind){
            CYPHER_RULE_PATH = FileUtils.getRealPath(CYPHER_RULE_PATH);
            FileUtils.fileNotExistsWithException(CYPHER_RULE_PATH);
        }

        if(!IS_DOCKER_IMPORT_PATH && isLoad){
            FileUtils.fileNotExistsWithException(CLASSES_CACHE_PATH);
            FileUtils.fileNotExistsWithException(METHODS_CACHE_PATH);
            FileUtils.fileNotExistsWithException(CALL_RELATIONSHIP_CACHE_PATH);
            FileUtils.fileNotExistsWithException(ALIAS_RELATIONSHIP_CACHE_PATH);
            FileUtils.fileNotExistsWithException(EXTEND_RELATIONSHIP_CACHE_PATH);
            FileUtils.fileNotExistsWithException(HAS_RELATIONSHIP_CACHE_PATH);
            FileUtils.fileNotExistsWithException(INTERFACE_RELATIONSHIP_CACHE_PATH);
        }
    }

    public static String getProperty(String key, String defaultValue, Properties props){
        return props.getProperty(key, defaultValue).trim();
    }

    public static boolean getBooleanProperty(String key, String defaultValue, Properties props){
        return Boolean.parseBoolean(getProperty(key, defaultValue, props));
    }

    public static int getIntProperty(String key, String defaultValue, Properties props){
        return Integer.parseInt(getProperty(key, defaultValue, props));
    }
}
