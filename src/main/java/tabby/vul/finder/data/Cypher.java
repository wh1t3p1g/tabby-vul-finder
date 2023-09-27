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
    private int depth = 8;
    private int limit = 10;
    private String name;
    private String type = "web";

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
        if(!pathBlacklists.isEmpty()){
            where = GlobalConfiguration.GSON.toJson(pathBlacklists);
        }
        sb.append(String.format("call %s(source, \"-\", sink, %d, true) yield path ", procedure, depth));
        if(where != null){
            sb.append(String.format("where none(n in nodes(path) where n.NAME0 in %s)", where));
        }
        sb.append("\n");
        sb.append("return path limit 1");
        return sb.toString();
    }
}
