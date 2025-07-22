package tabby.vul.finder.data;

import lombok.Data;
import tabby.vul.finder.config.GlobalConfiguration;

import java.util.Set;

/**
 * @author wh1t3p1g
 * @since 2023/3/12
 */
@Data
public class Cypher {

    private NodeCypher source;
    private NodeCypher sink;
    private Set<String> sourceBlacklists;
    private Set<String> pathBlacklists;
    private String procedure; // 目前仅支持 tabby.beta.findPath tabby.algo.findPath
    private String sinkState = null;
    private String direct = "-";
    private int depth = 8;
    private int limit = 10;
    private String name;
    private String type = "web";
    private boolean enable = true;
    private boolean depthFirst = false;

    public boolean isWeb(){
        return "web".equals(type);
    }

    public void addBlacklistToSource(String name){
        if(source == null) return;
        source.getBlacklists().add(name);
    }

    public void applyRuleSourceBlacklists(){
        if(sourceBlacklists != null && !sourceBlacklists.isEmpty()){
            addAllBlacklistToSource(sourceBlacklists);
        }
    }

    public void addAllBlacklistToSource(Set<String> names){
        if(source == null) return;
        source.getBlacklists().addAll(names);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(source);
        sb.append(sink);
        String where = null;
        pathBlacklists.remove("");
        if(!pathBlacklists.isEmpty()){
            where = GlobalConfiguration.GSON.toJson(pathBlacklists);
        }

        if(procedure.endsWith("WithState") && sinkState != null){
            sb.append(String.format("call %s(source, \"%s\", sink, \"%s\", %d, %s) yield path ", procedure, direct, sinkState, depth, depthFirst));
        }else{
            sb.append(String.format("call %s(source, \"%s\", sink, %d, %s) yield path ", procedure, direct, depth, depthFirst));
        }

        if(where != null && !where.isEmpty()){
            sb.append(String.format("where none(n in nodes(path) where n.NAME0 in %s)", where));
        }
        sb.append("\n");
        sb.append("return path limit 1");
        return sb.toString();
    }
}
