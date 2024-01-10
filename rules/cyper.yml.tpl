name: "web_to_code"
type: "web"
source:
  type: "source"
  endpoint: true
sink:
  type: "sink"
  sink: true
  vul: "CODE"
depth: 8
limit: 10
procedure: "tabby.algo.findPath"
sourceBlacklists: []
pathBlacklists:
  - "java.lang.Iterable#forEach"