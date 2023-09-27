package tabby.vul.finder.data;

import lombok.Data;
import tabby.vul.finder.config.GlobalConfiguration;

import java.util.HashSet;
import java.util.Set;

/**
 * @author wh1t3p1g
 * @since 2023/5/10
 */
@Data
public class NodeCypher {

    private String name = null;
    private String name0 = null;
    private String classname = null;
    private String signature = null;
    private boolean isEndpoint = false;
    private boolean isNettyEndpoint = false;
    private boolean isSerializable = false;
    private boolean isSetter = false;
    private boolean isGetter = false;
    private boolean isPublic = false;
    private boolean isStatic = false;
    private boolean isSink = false;
    private String vul = null;
    private int parameterSize = -1;
    private String type = null;

    private Set<String> blacklists = new HashSet<>();

    @Override
    public String toString() {
        Set<String> settings = new HashSet<>();
        if(name != null){
            settings.add(String.format("NAME: \"%s\"", name));
        }
        if(name0 != null){
            settings.add(String.format("NAME0: \"%s\"", name0));
        }

        if(classname != null){
            settings.add(String.format("CLASSNAME: \"%s\"", classname));
        }
        if(signature != null){
            settings.add(String.format("SIGNATURE: \"%s\"", signature));
        }
        if(vul != null){
            settings.add(String.format("VUL: \"%s\"", vul));
        }
        if(parameterSize != -1){
            settings.add(String.format("PARAMETER_SIZE: %d", parameterSize));
        }
        if(isEndpoint){
            settings.add(String.format("IS_ENDPOINT: %s", true));
        }
        if(isNettyEndpoint){
            settings.add(String.format("IS_NETTY_ENDPOINT: %s", true));
        }
        if(isSerializable){
            settings.add(String.format("IS_SERIALIZABLE: %s", true));
        }
        if(isSetter){
            settings.add(String.format("IS_SETTER: %s", true));
        }
        if(isGetter){
            settings.add(String.format("IS_GETTER: %s", true));
        }
        if(isPublic){
            settings.add(String.format("IS_PUBLIC: %s", true));
        }
        if(isStatic){
            settings.add(String.format("IS_STATIC: %s", true));
        }
        if(isSink){
            settings.add(String.format("IS_SINK: %s", true));
        }

        String properties = null;
        if(settings.size() > 0){
            properties = String.join(", ", settings);
        }

        String where = null;
        if(blacklists.size() > 0){
            where = GlobalConfiguration.GSON.toJson(blacklists);
        }

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("match (%s:Method ", type));
        if(properties != null){
            sb.append("{");
            sb.append(properties);
            sb.append("}");
        }
        sb.append(") ");
        if(where != null){
            sb.append(String.format("where not(%s.NAME0 in ", type));
            sb.append(where);
            sb.append(")");
        }
        sb.append("\n");
        return sb.toString();
    }
}
