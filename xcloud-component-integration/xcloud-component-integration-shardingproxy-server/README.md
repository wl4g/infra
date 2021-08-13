# XCloud Component for Sharding Proxy Server

> It's an enhanced package that integrates shardingsphere-proxy and shardingsphere-scaling

## 1. Compile building

- Step1: First, building of [`shardingsphere`](https://github.com/apache/shardingsphere)

```bash
git clone https://github.com/apache/shardingsphere.git
cd shardingsphere
git checkout 5.0.0-beta
mvn clean install -DskipTests -Dmaven.test.skip=true -T 2C
```

- Step2: Building of [`xcloud-component`](https://github.com/wl4g/xcloud-component)

```bash
# git clone https://github.com/wl4g/xcloud-component.git
cd xcloud-component
mvn clean install -DskipTests -Dmaven.test.skip=true -T 2C
```

- Step3: Importion demo data

Directories:

```text
├── demo_data
│   ├── group_sharding
│   │   ├── sharding1.jpg
│   │   ├── sharding2.jpg
│   │   └── userdb-sharding.sql
│   └── sharding
│       └── userdb-sharding.sql
```

> Notes: The example of non average slicing is not recommended for production (scenario: slicing according to different machine performance weight), because shardingsphere:5.0.0-alpha, It is recommended to use average sharding.


- Step4: Startup shardingsphere proxy(v4 and v5 Choose one)  

```bash
java -jar shardingproxy-{version}-bin.jar 3307 /example-conf/readwrite
# or:
# java -cp xxx com.wl4g.ShardingProxy 3307 /example-conf/readwrite
```

## 2. Testing scripts

```bash
$MYSQL_HOME/bin/mysql -h10.0.0.114 -P3307 -uroot -p123456
```

```sql
use userdb;
SELECT * FROM userdb.t_user;
INSERT INTO userdb.t_user (id, name) VALUES (10000000, 'user-insert-1111');
UPDATE userdb.t_user SET name='user-update-2222' WHERE id=10000000;
DELETE FROM userdb.t_user WHERE id=10000000;
```

## 3. Failover

> Notice: It has been officially implemented [org.apache.shardingsphere.dbdiscovery.mgr.MGRDatabaseDiscoveryType](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-db-discovery/shardingsphere-db-discovery-provider/shardingsphere-db-discovery-mgr/src/main/java/org/apache/shardingsphere/dbdiscovery/mgr/MGRDatabaseDiscoveryType.java), but the 5.0.0-beta is still very unstable. Therefore, at present, we still use the self implemented failover.

- `[MySQL5.7 Group Replication](https://dev.mysql.com/doc/refman/5.7/en/group-replication.html)` implementation theory:

```sql
SELECT
    rgm.CHANNEL_NAME AS channelName,
    rgm.MEMBER_ID AS nodeId,
    rgm.MEMBER_HOST AS nodeHost,
    rgm.MEMBER_PORT AS nodePort,
    rgm.MEMBER_STATE AS nodeState,
    @@read_only AS readOnly,
    @@super_read_only AS superReadOnly,(
    CASE (SELECT TRIM(VARIABLE_VALUE) FROM `performance_schema`.`global_status` WHERE VARIABLE_NAME = 'group_replication_primary_member')
      WHEN '' THEN 'UNKOWN'
      WHEN rgm.MEMBER_ID THEN 'PRIMARY'
      ELSE 'STANDBY' END
    ) AS nodeRole
FROM
    `performance_schema`.`replication_group_members` rgm
```

For example result:

```table
group_replication_applier  eb838b34-9deb-11eb-8677-c0b5d741e9d5  wanglsir-pro  13306 ONLINE  0  0  PRIMARY
group_replication_applier  05e9eb4f-9dec-11eb-8b2e-c0b5d741e9d5  wanglsir-pro  13307 ONLINE  0  0  STANDBY
group_replication_applier  3d4ed671-9dec-11eb-9723-c0b5d741e9d5  wanglsir-pro  13308 ONLINE  0  0  STANDBY
```

## 4. FQA

- 1. Can the same schema support different types of databases at the same time under read-write splitting and fragment splitting modes?

Under the same schemaName, multiple sharding databases must be the same. See source code: [org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData](https://github.com/apache/shardingsphere/blob/5.0.0-beta/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/metadata/ShardingSphereMetaData.java#L35) and [org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource](https://github.com/apache/shardingsphere/blob/5.0.0-beta/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/metadata/resource/ShardingSphereResource.java#L43)

- 2. How can the `/myShardingProxy/states/datanodes/mySchema` node in ZK disable data sources? Reference source code:

[DataSourceStatusRegistryService.java#loadDisabledDataSources()](https://github.com/apache/shardingsphere/blob/master/shardingsphere-governance/shardingsphere-governance-core/src/main/java/org/apache/shardingsphere/governance/core/registry/state/service/DataSourceStatusRegistryService.java#L44)

- 3. How do I configure failover? for example:

```config
cat server.yaml

props:
  failover-enable: true # Default by true
  # Failover admin dataSource configuration.
  # Notes: This configuration is used for read-write separation data source failover. Therefore, the same account
  #   password must be created for all master and slave databases before service startup.
  failover-configuration-json: |-
     {
         "inspectInitialDelayMs": 3000,
         "inspectMinDelayMs": 3000,
         "inspectMaxDelayMs": 10000,
         "adminDataSources": [{
             "schemaName": "userdb",
             "username": "root",
             "password": "123456",
             "mappings": [{
                 "internalAddr": "wanglsir-pro:13306",
                 "externalAddrs": [
                     "wl4g.debug:13306"
                 ]
             }, {
                 "internalAddr": "wanglsir-pro:13307",
                 "externalAddrs": [
                     "wl4g.debug:13307"
                 ]
             }, {
                 "internalAddr": "wanglsir-pro:13308",
                 "externalAddrs": [
                     "wl4g.debug:13308"
                 ]
             }]
         }]
     }
  # Notes: If failover is enabled and distributed governance mode is adopted, lock must be opened.
  lock-enabled: true # Default by false
```

> [Details refer to 'example-conf/sharding-readwrite/server.yaml'](src/main/resources/example-conf/sharding-readwrite/server.yaml)

| Attribute | Description |
|-|-|
| inspectInitialDelayMs | Monitor the initial start waiting time of the inspecting backend read/write dataSources group thread (ms). |
| inspectMinDelayMs | Monitor the min interval time inspecting read/write dataSources group thread (ms). |
| inspectMaxDelayMs | Monitor the max interval time inspecting read/write dataSources group thread (ms). |
| adminDataSources  | Admin dataSource configuration for inspection. |
| adminDataSources.schemaName | The virtual database schemaName corresponding to config-xx.yaml (Must be consistent). |
| adminDataSources.username | The account name of the data source grouped by the patrol database (some databases may be ordinary accounts, the query cluster state information no permission) |
| adminDataSources.password | Same as `adminDataSources.username` |
| adminDataSources.mappings.internalAddr | The access address of each data source library instance may be an external load balancing or proxy address (one-to-many) to external addresses. |
| adminDataSources.mappings.externalAddrs | The access address of each data source library instance may be an external load balancing or proxy address (many-to-one) to internal address. |

Notices:

- You can configure to enable or disable read-write failover as follows. `failover-enable: true|false`
- In the governance mode (cluster), the distributed lock must be enabled. It is disabled by default. &nbsp; `lock-enabled: true`
- Compatible with dataSources disabled in support registry center path: `/myShardingProxy/states/datanodes/mySchema`
