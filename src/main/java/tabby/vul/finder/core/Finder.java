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

    private static Set<String> GLOBAL_WEB_BLACKLIST
            = new HashSet<>(Arrays.asList(
            "org.apache.catalina.servlets.CGIServlet#doGet",
            "org.apache.catalina.manager.ManagerServlet#doGet",
            "org.apache.catalina.manager.host.HostManagerServlet#doGet",
            "org.apache.catalina.servlets.DefaultServlet#doPost",
            "org.apache.catalina.servlets.WebdavServlet#service",
            "org.apache.catalina.servlets.DefaultServlet#service",
            "org.apache.catalina.servlets.DefaultServlet#doHead",
            "org.apache.catalina.servlets.CGIServlet#service",
            "org.apache.catalina.servlets.DefaultServlet#doGet",
            "com.caucho.burlap.server.BurlapServlet#service",
            "com.caucho.hessian.server.HessianServlet#service",
            "org.apache.catalina.servlets.DefaultServlet#doPut",
            "org.apache.catalina.manager.HTMLManagerServlet#doPost",
            "org.apache.catalina.servlets.WebdavServlet#doPut",
            "org.apache.catalina.manager.ManagerServlet#doPut",
            "org.apache.catalina.manager.HTMLManagerServlet#doGet",
            "org.apache.catalina.servlets.WebdavServlet#doDelete",
            "org.apache.catalina.servlets.DefaultServlet#doDelete",
            "javax.servlet.http.HttpServlet#service",
            "javax.servlet.http.HttpServlet#doHead",
            "javax.servlet.http.HttpServlet#doGet",
            "javax.servlet.http.HttpServlet#doPost",
            "javax.servlet.http.HttpServlet#doPut",
            "org.apache.catalina.manager.host.HTMLHostManagerServlet#doPost"
    ));

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

            for(Cypher cypher:cyphers){
                try{
                    cypher.applyRuleSourceBlacklists();
                    String filepath = String.join(File.separator, output, cypher.getName()+"_result.txt");
                    try(FileOutputStream fos = new FileOutputStream(filepath)){
                        if(cypher.isWeb()){
                            cypher.addAllBlacklistToSource(GLOBAL_WEB_BLACKLIST);
                        }
                        int count = 0;
                        int limit = cypher.getLimit();
                        for(int i=0; i < limit; i++){
                            String currentCypher = cypher.toString();
                            log.debug(currentCypher);
                            List<PathValue> result = methodRefRepository.execute(currentCypher);
                            if(!result.isEmpty()){
                                Path path = result.get(0).asPath();
                                String head = output(path, fos);
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

    public String output(Path path, OutputStream os) throws IOException {
        StringBuilder sb = new StringBuilder();
        Iterator iterator = path.iterator();
        String ret = null;
        Node startNode = null;
        Node endNode = null;
        do{
            Path.Segment segment = (Path.Segment) iterator.next();
            startNode = segment.start();
            Relationship relationship = segment.relationship();
            endNode = segment.end();
            String startNodeName0 = startNode.get("NAME0").asString();
            if(ret == null){
                ret = startNodeName0;
            }
            sb.append(startNodeName0);
            if("CALL".equals(relationship.type())){
                sb.append("\n -[CALL]-> ");
            }else{
                sb.append(" -[ALIAS]-> ");
            }
        }while (iterator.hasNext());

        if(endNode != null){
            sb.append(endNode.get("NAME0").asString());
        }

        sb.append("\n");
        log.debug(sb.toString());
        sb.append("\n");
        os.write(sb.toString().getBytes());
        return ret;
    }
}
