### 简介：
使用SpringBoot+ES+RabbitMQ实现文档搜索功能，包含常用操作，没有业务代码干扰，拿来即可用
### 主要包含的内容:
1. ES创建Index Mapping的全过程（包含日期 数组映射，中文分词等）
2. ES对Document的增删改查与业务逻辑的结合
3. ES常用查询（包含boolQuery filter termQuery rangeQuery multiMatchQuery，聚合，分页排序，关键字高亮[可直接在html中展示]，数组查询，
时间范围查询等）
4. SpringBoot整合RabbitMQ，实现ES对Document的操作与业务逻辑的解耦
### 业务介绍：
1. 假设我们有一个文档管理系统，用户可以上传文档/修改文档/删除文档/对文档添加标签，现在我们需要对系统的文档进行搜索，
可以按照类型（ppt word excel）/作者搜索，按照文件名/摘要进行全文匹配，按照创建时间进行范围查询，按照文档标签（一个文档可以有多个标签）进行搜索。
2. 在用户添加文档后，我们需要对文档进行分析，获得文档摘要，用ES进行索引，所以我们引入了RabbitMQ来实现这一步。
3. **注意**我们的系统是文档管理系统，这里的文档和ES的Document是完全不同的两个概念，请注意区分！
### 代码结构：
* bus包，即为业务逻辑相关代码
* config包，ES和RabbitMQ相关配置类
* es.service包，ES相关代码
* mq.service包，MQ相关代码
* ESJson文件夹，ES Mapping的json数据
* ElasticsearchApplicationTests.java ES的相关测试，包含创建数据/查询
* DocController.java MQ的相关测试
### 使用说明：
1. 参看json文件的说明.txt，创建ES Index和Mapping
2. 执行ES测试，创建数据，测试查询
3. 执行MQ测试，测试MQ的功能
### 其他：
可以参看[这里](https://www.jianshu.com/p/198d4c3fbea5)，对整个ES的开发流程有简单说明


### 中文社区文档
https://www.elastic.co/guide/cn/elasticsearch/guide/current/index.html


# es-client-demo
spring boot2 整合elasticsearch 7.2 
https://github.com/baiczsy/elasticsearch-client-spring-boot-starter
https://github.com/intergrate-dev/elasticsearch-tool
https://github.com/baiczsy/spring-elasticsearch-client

https://blog.csdn.net/u013515384/article/details/84994763

https://www.cnblogs.com/WeidLang/p/10245659.html



RestHighLevelClient 模糊搜索
https://www.cnblogs.com/wangrudong003/p/10959525.html
https://blog.csdn.net/paditang/article/details/78802799
https://gitee.com/wzlee/ESClientRHL

https://www.elastic.co/guide/cn/elasticsearch/guide/current/_wildcard_and_regexp_queries.html




#### must、should、must_not
https://www.itsvse.com/forum.php?mod=viewthread&tid=6334&extra=&ordertype=1



https://blog.csdn.net/weixin_42393758/article/details/84581314



### Api(DSL)

https://www.jianshu.com/p/3cb205b5354a



```
list all indices
http://127.0.0.1:9200/_cat/indices

view fields type
http://127.0.0.1:9200/megacorp/_mapping

create index
curl -XPUT 'http://10.72.25.10:9200/books/' -d '{  
    "mappings": {
        "IT": {
            "properties": {
                "id": {"type": "keyword"},      
                "title": {"type": "keyword"},      
                "price": {"type": "float"},      
                "year": {"type": "integer"},      
                "description": {"type": "keyword"},      
                "tel": {"type": "keyword"},
                "d_val": {"type": "date", "format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"}
            }    
         }  
    }
}
```

批量加载数据
```
$ cat book.json
    {"index":{ "_index": "books", "_type": "IT", "_id": "1" }}
    {"id":"1","title":"Java Book","language":"java","author":"Bruce Eckel","price":70.20,"year":2007,"description":"Java must read", "tel":"15313016138", "d_val":"2018-11-01 12:25:36"}
    {"index":{ "_index": "books", "_type": "IT", "_id": "2" }}
    {"id":"2","title":"Java perform","language":"java","author":"John Li","price":46.50,"year":2012,"description":"permformance ..", "tel":"13548621254", "d_val":"2018-11-01 08:25:50"}
    {"index":{ "_index": "books", "_type": "IT", "_id": "3" }}
    {"id":"3","title":"Python compte","language":"python","author":"Tohma Ke","price":81.40,"year":2016,"description":"py ...", "tel":"13245687956", "d_val":"2018-11-01 19:30:20"}
    {"index":{ "_index": "books", "_type": "IT", "_id": "4" }}
    {"id":"4","title":"Python base","language":"python","author":"Tomash Si","price":54.50,"year": 2014,"description":"py base....", "tel":"aefda1567fdsa13", "d_val":"2018-09-01"}
    {"index":{ "_index": "books", "_type": "IT", "_id": "5" }}
    {"id":"5","title":"JavaScript high","language":"javascript","author":"Nicholas C.Zakas","price":66.40,"year":2012,"description":"JavaScript.....", "tel":"a14512dfa", "d_val":"2018-08-01"}

$ curl -XPOST "http://10.72.25.10:9200/_bulk?pretty" --data-binary @book.json
```

delete index
DELETE http://10.72.25.10:9200/books

#### 1. insert
```
PUT     http://127.0.0.1:9200/megacorp/employee/3

requet body
{
    "first_name": "Douglas",
    "last_name": "Fir",
    "age": 35,
    "about": "I like to build cabinets",
    "interests": [
        "forestry"
    ]
}
```

#### 2. search
GET     /megacorp/employee/1


GET     http://127.0.0.1:9200/megacorp/employee/3


GET /megacorp/employee/_search
```
{

    "query": {
        "match_all": {}
    }
}


{
    "query" : {
        "match" : {
            "last_name" : "Smith"
        }
    }
}
or
{  
    "size":10,
    "query": {
        "bool" : {
           "must" : {
              "match" : {
                "last_name" : "Smith"
              }
           }
         }
    }
}

```

不包含
```
{  
    "size":10,
    "query": {
        "bool" : {
           "must_not" : {
              "match" : {
                "last_name" : "Smith"
              }
           }
         }
    }
}

{
    "query": {
        "range": {
            "age": {
                "gt": 30
            }
        }
    }
}
```

多条件

```
{
  "query": { 
    "bool": { 
      "must": [
        { "match": { "title":   "Search"        }},
        { "match": { "content": "Elasticsearch" }}
      ],
      "filter": [ 
        { "term":  { "status": "published" }},
        { "range": { "publish_date": { "gte": "2015-01-01" }}}
      ]
    }
  }
}
```

```
{
    "size": 10,
    "query": {
        "bool": {
            "must": [
                {
                    "match": {
                        "last_name": "Smith"
                    }
                },
                {
                    "match": {
                        "age": "25"
                    }
                }
            ]
        }
    }
}

{
    "query": {
        "bool": {
            "must": [
                {
                    "match": {
                        "last_name": "Smith"
                    }
                }
            ],
            "filter": [
                {
                    "range": {
                        "age": {
                            "gte": "25"
                        }
                    }
                }
            ]
        }
    }
}
```

高亮
```
{
    "query" : {
        "match_phrase" : {
            "字段" : "搜索值"
        }
    },
    "highlight": {
        "fields" : {
            "字段" : {}
        }
    }
}
```


统计
1. 基本统计
GET     http://127.0.0.1:9200/megacorp/employee/_search
```
{
    "size": 0, # 不加，hits.hits有记录
    "aggs": {
        "grades_stat": {
            "stats": {
                "field": "age"
            }
        }
    }
}
```

分组聚合
```
{
    "size": 0,
    "aggs": {
        "user_type": {
            "terms": {
                "field": "age"
            }
        }
    }
}

{  
   "query": {  
     "match": {  
       "last_name": "Zhong"  
     }  
   },  
   "aggs": {  
     "all_interests": {  
       "terms": {  
         "field": "interests"  
       }  
     }  
   }  
 }


```


2. 高级统计
```
{  
    "size": 0,
    "aggs": {
        "grades_stat": {
            "extended_stats": {
                "field":"price"
            }
        }
    }
}
```


### 全文检索
GET     http://127.0.0.1:9200/megacorp/employee/_search

request body
```
{
    "query": {
        "match": {
            "about": "rock climbing"
        }
    }
}
```


查询DSL进阶

https://blog.csdn.net/icool_ali/article/details/81666628
https://www.cnblogs.com/miqi1992/p/5708553.html
https://blog.csdn.net/fanrenxiang/article/details/86477019

match 匹配 分词

multi_match多值匹配
{
  "query": {
    "multi_match": {
      "query": "值",
      "fields": [
        "name",
        "age"
      ]
    }
  }
}

term 精确匹配 不分词

{
  "query": {
    "term": {
      "ip": "值"
    }
  }
}



### Prometheus+Grafana
reference: https://www.cnblogs.com/niechen/p/10150004.html

mvn clean package docker:build


#### prepare
pom, application.properties, *Application.java(embed grafana), docker-compose.yml+prom.yml(register).
At last, run app and validate url "http://localhost:8086/actuator/prometheus"

#### prometheus
docker-compose up -d 
http://192.168.122.1:9090/targets

#### grafana
in browser, add datasource, and import jvm-micrometer

https://grafana.com/dashboards/4701
(https://grafana.com/grafana/dashboards)
https://grafana.com/docs/
https://grafana.com/grafana/plugins



#### stop service
docker-compose stop site-monitor-service


### logstash
```text
input {
 
        file {
                start_position => end ### 读文件的位子
                path => "/root/projects/fp-api/log/fp-api.log"
                type => "type1" ### 用去输出到es时判断存入哪个索引
                codec => multiline {
                        negate => true ### 是否匹配到
                        pattern => "(?<datetime>\d{4}-\d{2}-\d{2}\s\d{2}:\d{2}:\d{2}.\d{3})  INFO %{NUMBER:thread} --- %{SYSLOG5424SD:task} %{JAVACLASS}\s*: %{SYSLOG5424SD:module}\s*%{GREEDYDATA:msg}" ### 匹配的正则
                        what => "previous" ###将没匹配到的合并到上一条，可选previous或next, previous是合并到匹配的上一行末尾
                        max_lines => 1000 ### 最大允许的行
                        max_bytes => "10MiB" ### 允许的大小
                        auto_flush_interval => 30 ### 如果在规定时候内没有新的日志事件就不等待后面的日志事件
               }
        }
 
 
        file {
​                start_position => end
​                path => "/root/projects/fp-acq/log/fp-acq.log"
​                type => "type2"
​                codec => multiline {
​                        pattern => "(?<datetime>\d{4}-\d{2}-\d{2}\s\d{2}:\d{2}:\d{2}.\d{3})  INFO %{NUMBER:thread} --- %{SYSLOG5424SD:task} %{JAVACLASS}\s*: %{SYSLOG5424SD:module}\s*%{GREEDYDATA:msg}"
​                        negate => true
​                        what => "previous"
​                }
​        }
 
}
 
 
filter{
        grok{
                match => {
                        "message" => "(?<datetime>\d{4}-\d{2}-\d{2}\s\d{2}:\d{2}:\d{2}.\d{3})  INFO %{NUMBER:thread} --- %{SYSLOG5424SD:task} %{JAVACLASS:javaclass}\s*: %{SYSLOG5424SD:module}\s*%{GREEDYDATA:msg}"
                 }
        } ### 通过grok匹配内容并将
     
        date{
                match => ["datetime","yyyy-MM-dd HH:mm:ss.SSS","yyyy-MM-dd HH:mm:ss.SSSZ"]
                target => "@timestamp"
        } ### 处理时间
}
 
output {
 
    if [type] == "type1" {
        elasticsearch {
​        hosts => "192.168.1.158"
​        index => "fp_log_type1"
          }
   }
 
    if [type] == "type2" {
​        elasticsearch {
​        hosts => "192.168.1.158"
​        index => "fp_log_typr2"
          }
 
    }
}
```

