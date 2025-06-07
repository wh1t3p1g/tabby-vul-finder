package tabby.vul.finder;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
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
		Command command = new Command();
		JCommander.newBuilder()
				.addObject(command)
				.build()
				.parse(args);

		if(command.isQuery()){
			isFind = true;
			GlobalConfiguration.CYPHER_RESULT_DIRECTORY_PREFIX = command.prefix;
			GlobalConfiguration.CYPHER_RULE_PATH = command.queryRulePath;
		}else if(command.isLoad()){
			isLoad = true;
			GlobalConfiguration.CSV_PATH = command.csvFilePath;
		}else{
			command.printHelp();
			return;
		}

		if(command.configFilePath != null){
			GlobalConfiguration.CONFIG_FILE_PATH = command.configFilePath;
		}

		GlobalConfiguration.init();
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

	@Slf4j
	static final class Command {

		@Parameter(names = {"--prefix", "-p"}, description = "可选，结果目录前缀，无特殊意义，可任意指定。")
		public String prefix = "result";

		@Parameter(names = {"--query", "-q"}, description = "指代当前为查询模式，提供对应的cypher规则文件路径。")
		public String queryRulePath = null;

		@Parameter(names={"--config"}, description = "数据库配置文件地址。")
		public String configFilePath = null;

		@Parameter(names={"--load", "-l"}, description = "导入csv文件到图数据库中。")
		public String csvFilePath = null;

		@Parameter(names={"--help", "-h"})
		public boolean help = false;

		public boolean isLoad(){
			return csvFilePath != null;
		}

		public boolean isQuery(){
			return queryRulePath != null;
		}

		public void printHelp(){
			log.error("You can try following commands:");
			log.error("java -jar tabby-vul-finder.jar --query /path/to/neo4j/cypher/rules.yml --prefix projectName --config /path/to/db.properties");
			log.error("java -jar tabby-vul-finder.jar --load /path/to/load/to/neo4j --config /path/to/db.properties");
		}
	}
}
