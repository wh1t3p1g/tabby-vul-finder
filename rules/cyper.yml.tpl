name: "netty_to_serialize"
type: "netty"
source:
  type: "source|sink"
  nettyEndpoint: boolean
  endpoint: boolean
  name0: string
sink:
  type: "sink"
  sink: true
  vul: "SERIALIZE"
depth: 8
pathBlacklists:
  - "java.lang.Iterable#iterator"
  - "java.util.Iterator#hasNext"