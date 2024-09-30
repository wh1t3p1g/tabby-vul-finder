package tabby.vul.finder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import tabby.vul.finder.config.GlobalConfiguration;
import tabby.vul.finder.core.Finder;
import tabby.vul.finder.core.Loader;
import tabby.vul.finder.util.FileUtils;

import java.io.FileNotFoundException;

@Slf4j
@SpringBootApplication
@EntityScan({"tabby.vul.finder.dal.entity"})
@EnableNeo4jRepositories("tabby.vul.finder.dal.repository")
public class App {

	@Autowired
	private Loader loader;

	@Autowired
	private Finder finder;

	private static boolean isLoad = false;
	private static boolean isFind = false;

	public static void main(String[] args) throws FileNotFoundException {
		if(args.length == 3 && "query".equals(args[0])){
			isFind = true;
			GlobalConfiguration.CYPHER_RESULT_DIRECTORY_PREFIX = args[1];
			GlobalConfiguration.CYPHER_RULE_PATH = args[2];
		}else if(args.length == 2 && "load".equals(args[0])){
			isLoad = true;
			GlobalConfiguration.CSV_PATH = args[1];
		}else{
			log.error("java -jar tabby-vul-finder.jar query projectName /path/to/neo4j/cypher/rules.yml");
			log.error("java -jar tabby-vul-finder.jar load /path/to/load/to/neo4j");
			return;
		}
		GlobalConfiguration.initConfig(isLoad, isFind);
		SpringApplication.run(App.class, args).close();
	}


	@Bean
	CommandLineRunner run(){
		return args -> {
			try{
				if(isLoad){
					loader.load();
				}
				if(isFind){
//					if(FileUtils.fileNotExists(GlobalConfiguration.RULES_PATH)){
//						FileUtils.createDirectory(GlobalConfiguration.RULES_PATH);
//					}
					if(FileUtils.fileNotExists(GlobalConfiguration.CYPHER_RESULT_DIRECTORY)){
						FileUtils.createDirectory(GlobalConfiguration.CYPHER_RESULT_DIRECTORY);
					}
					finder.find();
				}
			}catch (IllegalArgumentException e){
				log.error(e.getMessage() + ", Please check your settings.");
			}
			log.info("Done. Bye!");
		};
	}
}
