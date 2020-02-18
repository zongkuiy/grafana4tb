## Grafana Support Thinsboard
A backend project for [Grafana](https://github.com/grafana/grafana) connecting to [ThingsBoard](https://github.com/thingsboard/thingsboard) server using [Simple-Json-Datasource](https://github.com/grafana/simple-json-datasource)(a grafana datasource plugin).

#### Build

 - Build and maven install the thingsboard maven dependencies 

 - Set the thingsboard version in grafana4tb  pom.xml  
```
        <thingsboard.version>2.4.1-SNAPSHOT</thingsboard.version>

```

- maven build
```
        mvn clean package
```

#### Start Server
 - Copy the thingsboard configuration file(thingsboard.yml) and modify the bind port if needed

- Start the server using the configuration file on above step
```
        java -jar grafana4tb-0.0.1-SNAPSHOT.jar --spring.config.location=/your/conf/dir/ 
```

#### Install the simple json datasource plugin on grafana

- Refer https://github.com/grafana/simple-json-datasource

#### Config the datasource 

- Grafana Dashboard -> Configuration -> Datasources
- Add datasoure -> filter 'simplejson'
- Configuration as below:


Field | Value
---|---
HTTP.URL | http://<grafana4tb_host>:<grafana4tb_port>/grafana/<device_token>
HTTP.ACCESS | server/client both works
Auth | default
Basic Auth Details | default


    
