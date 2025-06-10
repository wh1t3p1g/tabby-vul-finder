package tabby.vul.finder.core;

import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.internal.value.PathValue;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tabby.vul.finder.config.GlobalConfiguration;
import tabby.vul.finder.dal.repository.MethodRefRepository;
import tabby.vul.finder.data.Cypher;
import tabby.vul.finder.util.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author wh1t3p1g
 * @since 2023/3/8
 */
@Slf4j
@Component
public class Finder {

    @Autowired
    private MethodRefRepository methodRefRepository;

    private String output;

    public void init(){
        LocalDateTime current = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        String directory = current.format(formatter);
        output = String.join(File.separator, GlobalConfiguration.CYPHER_RESULT_DIRECTORY, GlobalConfiguration.CYPHER_RESULT_DIRECTORY_PREFIX+"_"+ directory);
        if(!FileUtils.fileExists(output)){
            FileUtils.createDirectory(output);
        }else{
            clean(output);
        }
    }

    public static void clean(String directory){
        try {
            File cacheDir = new File(directory);
            File[] files = cacheDir.listFiles();
            if(files != null){
                for(File file: files){
                    String name = file.getName();
                    if(name.endsWith(".txt")){
                        Files.deleteIfExists(file.toPath());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void find(){
        try{
            init();
            Set<Cypher> cyphers = FileUtils.getYamlContent(GlobalConfiguration.CYPHER_RULE_PATH);
            if(cyphers == null || cyphers.isEmpty()){
                log.info("Cypher list size is zero.");
                return;
            }

            Set<String> globalWebSourceBlacklist = new HashSet<>(Arrays.asList(GlobalConfiguration.CYPHER_WEB_SOURCE_BLACKLIST));
            Set<String> globalWebPathBlacklist = new HashSet<>(Arrays.asList(GlobalConfiguration.CYPHER_WEB_PATH_BLACKLIST));
            for(Cypher cypher:cyphers){
                if(!cypher.isEnable()) continue;
                try{
                    cypher.applyRuleSourceBlacklists();
                    String filepath = String.join(File.separator, output, cypher.getName()+"_result.cypher");
                    try(FileOutputStream fos = new FileOutputStream(filepath)){
                        if(cypher.isWeb()){
                            cypher.addAllBlacklistToSource(globalWebSourceBlacklist);
                            cypher.getPathBlacklists().addAll(globalWebPathBlacklist);
                        }
                        int count = 0;
                        int limit = cypher.getLimit();
                        log.info("Start Cypher {}.", cypher.getName());
                        for(int i=0; i < limit; i++){
                            String currentCypher = cypher.toString();
                            log.debug(currentCypher);
                            List<PathValue> result = new ArrayList<>();

                            try{
                                result = methodRefRepository.execute(currentCypher);
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            if(!result.isEmpty()){
                                Path path = result.get(0).asPath();
                                String head = output(path, cypher.getDirect(), fos);
                                log.info("<{}> Found {}", cypher.getName(), head);
                                if(head != null){
                                    cypher.addBlacklistToSource(head);
                                    cypher.getPathBlacklists().add(head);
                                }
                                count++;
                            }else{
                                break;
                            }
                        }
                        log.info("Found {} path for {}", count, cypher.getName());
                    }
                }catch (Exception e){
                    log.error(e.getMessage());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String output(Path path, String direct, OutputStream os) throws IOException {
        StringBuilder sb = new StringBuilder();
        Iterator<Path.Segment> iterator = path.iterator();
        String ret = null;
        Node startNode = null;
        Node endNode = null;
        List<String> nodes = new LinkedList<>();
        do{
            Path.Segment segment =  iterator.next();
            startNode = segment.start();
            Relationship relationship = segment.relationship();
            endNode = segment.end();
            if(ret == null){
                ret = startNode.get("NAME0").asString();
            }

            if(startNode != null){
                nodes.add(startNode.get("NAME0").asString());
            }

            if(relationship != null){
                if("CALL".equals(relationship.type())){
                    nodes.add("[CALL]");
                }else{
                    nodes.add("[ALIAS]");
                }
            }
        }while (iterator.hasNext());

        if(endNode != null){
            nodes.add(endNode.get("NAME0").asString());
        }

        int length = nodes.size();
        sb.append("/*\n");
        if("<".equals(direct)){
            for(int i=length-1;i>=0;i--){
                String node = nodes.get(i);
                if(node.equals("[CALL]")){
                    sb.append(String.format("\n -%s-> ", node));
                } else if(node.equals("[ALIAS]")){
                    sb.append(String.format(" -%s-> ", node));
                }else{
                    sb.append(node);
                }
            }
        }else{
            for(int i=0;i<length;i++){
                String node = nodes.get(i);
                if(node.equals("[CALL]")){
                    sb.append(String.format("\n -%s-> ", node));
                } else if(node.equals("[ALIAS]")){
                    sb.append(String.format(" -%s-> ", node));
                }else{
                    sb.append(node);
                }
            }
        }

        sb.append("\n");
        log.debug(sb.toString());
        sb.append("*/\n");
        os.write(sb.toString().getBytes());

        generate(nodes, direct, os);
        return ret;
    }

    public void generate(List<String> nodes, String direct, OutputStream os) throws IOException {
        StringBuilder sb = new StringBuilder();
        int length = nodes.size();
        if("<".equals(direct)){
            for(int i=length-1;i>=0;i--){
                String node = nodes.get(i);
                if(node.equals("[CALL]")){
                    sb.append(" -[:CALL]-> ");
                } else if(node.equals("[ALIAS]")){
                    sb.append(" -[:ALIAS]-> ");
                }else{
                    if(length-1 == i){
                        sb.append(String.format("match path=(m%d:Method {NAME0:\"%s\"})\n", i, node));
                    }else{
                        sb.append(String.format("(m%d:Method {NAME0:\"%s\"})\n", i, node));
                    }
                }
            }
        }else{
            for(int i=0;i<length;i++){
                String node = nodes.get(i);
                if(node.equals("[CALL]")){
                    sb.append(" -[:CALL]-> ");
                } else if(node.equals("[ALIAS]")){
                    sb.append(" -[:ALIAS]-> ");
                }else{
                    if(0 == i){
                        sb.append(String.format("match path=(m%d:Method {NAME0:\"%s\"}) \n", i, node));
                    }else{
                        sb.append(String.format("(m%d:Method {NAME0:\"%s\"}) \n", i, node));
                    }
                }
            }
        }
        sb.append("return path");
        log.debug(sb.toString());
        sb.append("\n");
        sb.append("\n");
        os.write(sb.toString().getBytes());
    }

}
