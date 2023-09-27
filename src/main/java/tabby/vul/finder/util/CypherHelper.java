package tabby.vul.finder.util;

import tabby.vul.finder.config.GlobalConfiguration;

import java.util.Set;

/**
 * @author wh1t3p1g
 * @since 2023/3/12
 */
public class CypherHelper {

    public static String replace(String cypher, String identifier, Set<String> blacklist){
        String value = null;
        if(!blacklist.isEmpty()){
            value = GlobalConfiguration.GSON.toJson(blacklist);
        }

        if(value == null){
            return cypher.replace(identifier , "");
        }

        String where = null;

        switch (identifier){
            case "$SOURCE_WHERE":
                where = String.format("where not(source.NAME0 in %s)", value);
                break;
            case "$SINK_WHERE":
                where = String.format("where not(sink.NAME0 in %s)", value);
                break;
            case "$PATH_WHERE":
                where = String.format("where none(n in nodes(path) where n.NAME0 in %s)", value);
                break;
        }

        if(where == null){
            cypher = cypher.replace(identifier, "");
        }else{
            cypher = cypher.replace(identifier, where);
        }

        return cypher;
    }



}
