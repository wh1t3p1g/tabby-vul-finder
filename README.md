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

## #2 命令

```bash
java -jar tabby-vul-finder.jar query project_name
java -jar tabby-vul-finder.jar load /path/to/cache
```

- query 用于查询图数据库，并输出结果到 result 目录，project_name 可以任意指定.
- load  用于载入 tabby 生成的 csv 文件到 neo4j 图数据库中