package tabby.vul.finder.dal.repository;

import org.neo4j.driver.internal.value.PathValue;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tabby.vul.finder.dal.entity.MethodEntity;

import java.util.List;

/**
 * @author wh1t3P1g
 * @since 2020/10/10
 */
@Repository
public interface MethodRefRepository extends Neo4jRepository<MethodEntity, String> {

    @Query("CALL apoc.periodic.iterate(\"CALL apoc.load.csv('file://\"+$path+\"', " +
            "{header:true, ignore: ['IS_INITIALED', 'IS_ACTION_INITIALED', 'IS_BODY_PARSE_ERROR', 'IS_RETURN_CONSTANT_TYPE'], mapping:{ " +
            "IS_SINK: {type:'boolean'}, " +
            "IS_SOURCE: {type:'boolean'}, " +
            "IS_CONTAINS_SOURCE: {type:'boolean'}, " +
            "IS_STATIC: {type:'boolean'}, " +
            "IS_ENDPOINT: {type:'boolean'}, " +
            "IS_NETTY_ENDPOINT: {type:'boolean'}, " +
            "IS_RPC: {type:'boolean'}, " +
            "IS_DAO: {type:'boolean'}, " +
            "ISMRPC: {type:'boolean'}, " +
            "HAS_PARAMETERS:{type:'boolean'}, " +
            "IS_FROM_ABSTRACT_CLASS: { type: 'boolean'}, " +
            "IS_GETTER:{type:'boolean'}, " +
            "IS_SETTER:{type:'boolean'}, " +
            "IS_PUBLIC:{type:'boolean'}, " +
            "IS_ABSTRACT:{type:'boolean'}, " +
            "HAS_DEFAULT_CONSTRUCTOR:{type:'boolean'}, " +
            "IS_ACTION_CONTAINS_SWAP:{type:'boolean'}, " +
            "IS_CONTAINS_OUT_OF_MEM_OPTIONS:{type:'boolean'}, " +
            "IS_IGNORE: { type: 'boolean'}, IS_SERIALIZABLE:{type:'boolean'}, " +
            "MODIFIERS:{type:'int'}, PARAMETER_SIZE:{type:'int'}}}) YIELD map AS row RETURN row\", \"MERGE(m:Method {ID:row.ID} ) ON CREATE SET m = row\", {batchSize:10000, iterateList:true, parallel:true}) yield total")
    void loadMethodRefFromCSV(String path);

    @Query("CALL apoc.periodic.iterate(\"CALL apoc.load.csv('file://\"+$path+\"', " +
            "{header:true, mapping:{IS_CALLER_THIS_FIELD_OBJ:{type:'boolean'}} }) YIELD map AS row RETURN row\"," +
            "\"MATCH ( m1:Method {ID:row.SOURCE} )" +
            "MATCH ( m2:Method {ID:row.TARGET }) " +
            "MERGE (m1)-[e:CALL {ID:row.ID, LINE_NUM:row.LINE_NUM, " +
            "INVOKER_TYPE:row.INVOKER_TYPE, " +
            "POLLUTED_POSITION:row.POLLUTED_POSITION, " +
            "TYPES:row.TYPES, " +
//            "CONSTANTS:row.CONSTANTS, " +
            "IS_CALLER_THIS_FIELD_OBJ:row.IS_CALLER_THIS_FIELD_OBJ}]->(m2)\", " +
            "{batchSize:10000, iterateList:true, parallel:false}) yield total")
    void loadCallEdgeFromCSV(String path);

    @Query("CALL apoc.periodic.iterate(\"CALL apoc.load.csv('file://\"+$path+\"', {header:true}) YIELD map AS row RETURN row\",\"MATCH ( m1:Method {ID:row.SOURCE} ) MATCH ( m2:Method {ID:row.TARGET }) MERGE (m1)-[e:ALIAS {ID:row.ID}]-(m2)\", {batchSize:10000, iterateList:true, parallel:false}) yield total")
    void loadAliasEdgeFromCSV(String path);


    @Query("call apoc.cypher.run($cypher, null) yield value return value.path")
    List<PathValue> execute(String cypher);
}