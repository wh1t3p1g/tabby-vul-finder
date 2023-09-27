package tabby.vul.finder.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tabby.vul.finder.dal.service.ClassService;
import tabby.vul.finder.dal.service.MethodService;

import java.util.concurrent.TimeUnit;

/**
 * @author wh1t3p1g
 * @since 2023/3/8
 */
@Slf4j
@Component
public class Loader {

    @Autowired
    private ClassService classService;

    @Autowired
    private MethodService methodService;

    public void load(){
        long start = System.nanoTime();
        classService.clear(); // clean old data
        log.info("Try to load data.");
        methodService.importMethodRef();
        classService.importClassRef();
        classService.buildEdge();
        classService.statistic();

        long time = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start);
        log.info("Cost {} min {} seconds."
                , time/60, time%60);
    }
}
