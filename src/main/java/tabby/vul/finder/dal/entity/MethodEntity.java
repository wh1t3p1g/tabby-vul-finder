package tabby.vul.finder.dal.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 * @author wh1t3P1g
 * @since 2021/4/25
 */
@Data
@Node("Method")
public class MethodEntity {

    @Id
    private String id;

    private String name;

    private String signature;
}
