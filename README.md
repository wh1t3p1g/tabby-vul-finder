# tabby-vul-finder

## #0 简介
A vul-finder for [tabby](https://github.com/wh1t3p1g/tabby)

本项目用于动态生成配置后的 cypher 语句，查询 web 类的漏洞链路。

## #1 用法

生成jar文件
```bash
mvn clean package -DskipTests
```
修改配置文件 db.properties 里的 neo4j 数据库配置

另外，这里依赖 [tabby-path-finder](https://github.com/wh1t3p1g/tabby-path-finder)，需要在neo4j中加入当前依赖。

## #2 使用方法

参数：
- --config configFilePath：【可选】用于指定数据库配置文件地址，不指定则默认读取`config/db.properites`文件；
- --prefix/-p prefix：【可选】用于指定生成结果的目录名前缀，无特殊含义，不指定则默认为`result`前缀；
- --query/-q cypherRulePath：用于指定查询模式，根据提供的规则文件自动化输出可疑链路；
- --load/-l csvFilePath：用于导入csv文件到图数据库中；

使用示例：
```bash
java -jar tabby-vul-finder.jar --query rule/path.yml --prefix project_name
java -jar tabby-vul-finder.jar --load /path/to/csv
```