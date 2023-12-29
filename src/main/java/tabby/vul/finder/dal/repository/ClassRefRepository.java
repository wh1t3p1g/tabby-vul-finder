package tabby.vul.finder.dal.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import tabby.vul.finder.dal.entity.ClassEntity;


/**
 * @author wh1t3P1g
 * @since 2020/10/10
 */
@Repository
public interface ClassRefRepository extends Neo4jRepository<ClassEntity, String> {

    @Query("CALL apoc.periodic.iterate(\"CALL apoc.load.csv('file://\"+$path+\"', " +
            "{header:true, ignore: ['IS_PHANTOM','IS_INITIALED', 'FIELDS'], mapping:{ " +
            "IS_INTERFACE: {type:'boolean'}, " +
            "IS_PUBLIC: {type:'boolean'}, " +
            "IS_ABSTRACT: {type:'boolean'}, " +
            "HAS_SUPER_CLASS: {type:'boolean'}, " +
            "HAS_INTERFACES: {type:'boolean'}, " +
            "IS_STRUTS_ACTION: {type:'boolean'}, " +
            "HAS_DEFAULT_CONSTRUCTOR: {type:'boolean'}, " +
            "IS_SERIALIZABLE:{type:'boolean'}}}) YIELD map AS row RETURN row\",\"MERGE (c:Class {NAME:row.NAME}) ON CREATE SET c = row\", {batchSize:10000, iterateList:true, parallel:true}) yield total")
    void loadClassRefFromCSV(String path);

    @Query("CALL apoc.periodic.iterate(\"CALL apoc.load.csv('file://\"+$path+\"', {header:true}) YIELD map AS row RETURN row\",\"MATCH( c1:Class {ID:row.SOURCE} ) MATCH ( c2:Class { ID:row.TARGET } ) MERGE (c1) -[e:EXTENDS { ID:row.ID }] -> (c2)\", {batchSize:10000, iterateList:true, parallel:false}) yield total")
    void loadExtendEdgeFromCSV(String path);

    @Query("CALL apoc.periodic.iterate(\"CALL apoc.load.csv('file://\"+$path+\"', {header:true}) YIELD map AS row RETURN row\",\"MATCH( c1:Class {ID:row.SOURCE} ) MATCH ( c2:Class { ID:row.TARGET } ) MERGE (c1) -[e:INTERFACE { ID:row.ID }] -> (c2)\", {batchSize:10000, iterateList:true, parallel:false}) yield total")
    void loadInterfacesEdgeFromCSV(String path);

    @Query("CALL apoc.periodic.iterate(\"CALL apoc.load.csv('file://\"+$path+\"', {header:true}) YIELD map AS row RETURN row\",\"MATCH(c:Class{ID:row.CLASS_ID}) MATCH(m:Method { ID:row.METHOD_REF }) MERGE (c) -[e:HAS { ID:row.ID }]-> (m)\", {batchSize:10000, iterateList:true, parallel:false}) yield total")
    void loadHasEdgeFromCSV(String path);

    @Query("CALL apoc.periodic.iterate(\"match (n) return n\",\"delete n\", {batchSize:100000, iterateList:true, parallel:false}) yield total")
    int deleteAllNodes();

    @Query("CALL apoc.periodic.iterate(\"match ()-[r]->() return r\",\"delete r\", {batchSize:100000, iterateList:true, parallel:false}) yield total")
    int deleteAllEdges();

    @Query("CALL apoc.periodic.iterate(\"match (n) return n\",\"detach delete n\", {batchSize:100000, iterateList:true, parallel:false}) yield total")
    int deleteAllData();

    @Query("match (n) return count(n)")
    int countAllNode();

    @Query("match p=()-->() return count(p)")
    int countAllEdge();
}
