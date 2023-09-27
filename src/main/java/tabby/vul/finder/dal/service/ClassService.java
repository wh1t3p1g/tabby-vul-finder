package tabby.vul.finder.dal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tabby.vul.finder.config.GlobalConfiguration;
import tabby.vul.finder.dal.repository.ClassRefRepository;
import tabby.vul.finder.dal.repository.MethodRefRepository;
import tabby.vul.finder.util.FileUtils;

/**
 * @author wh1t3P1g
 * @since 2021/3/29
 */
@Slf4j
@Service
public class ClassService {

    @Autowired
    private ClassRefRepository classRefRepository;
    @Autowired
    private MethodRefRepository methodRefRepository;


    public void clear(){
        log.info("Start to clean old data.");
//        classRefRepository.deleteAllEdges();
//        classRefRepository.deleteAllNodes();
        classRefRepository.deleteAllData();
        log.info("Clean old data. Done!");
    }

    public void statistic(){
        int countAllNode = classRefRepository.countAllNode();
        int countAllEdge = classRefRepository.countAllEdge();
        log.info("Load {} nodes, {} edges.", countAllNode, countAllEdge);
    }

    public void importClassRef(){
        log.info("Save Class Node");
        classRefRepository.loadClassRefFromCSV(
                FileUtils.getWinPath(GlobalConfiguration.CLASSES_CACHE_PATH));
    }
    public void buildEdge(){
        log.info("Save Extend relationship");
        classRefRepository.loadExtendEdgeFromCSV(
                FileUtils.getWinPath(GlobalConfiguration.EXTEND_RELATIONSHIP_CACHE_PATH));

        log.info("Save Interface relationship");
        classRefRepository.loadInterfacesEdgeFromCSV(
                FileUtils.getWinPath(GlobalConfiguration.INTERFACE_RELATIONSHIP_CACHE_PATH));

        log.info("Save Has relationship");
        classRefRepository.loadHasEdgeFromCSV(
                FileUtils.getWinPath(GlobalConfiguration.HAS_RELATIONSHIP_CACHE_PATH));

        log.info("Save Call relationship");
        methodRefRepository.loadCallEdgeFromCSV(
                FileUtils.getWinPath(GlobalConfiguration.CALL_RELATIONSHIP_CACHE_PATH));

        log.info("Save Alias relationship");
        methodRefRepository.loadAliasEdgeFromCSV(
                FileUtils.getWinPath(GlobalConfiguration.ALIAS_RELATIONSHIP_CACHE_PATH));
    }

}
