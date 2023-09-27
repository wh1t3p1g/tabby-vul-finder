package tabby.vul.finder.dal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tabby.vul.finder.config.GlobalConfiguration;
import tabby.vul.finder.dal.repository.MethodRefRepository;
import tabby.vul.finder.util.FileUtils;

/**
 * @author wh1t3P1g
 * @since 2021/3/29
 */
@Slf4j
@Service
public class MethodService {

    @Autowired
    private MethodRefRepository methodRefRepository;

    public void importMethodRef(){
        log.info("Save Method Node");
        methodRefRepository.loadMethodRefFromCSV(
                FileUtils.getWinPath(GlobalConfiguration.METHODS_CACHE_PATH));
    }

    public MethodRefRepository getRepository(){
        return methodRefRepository;
    }
}
