# Native Canal Deployment

- [Refer to: Canal-Server for MySQL configuration](https://github.com/alibaba/canal/wiki/QuickStart)
- [Refer to: Canal-Server for Docker docs](https://github.com/alibaba/canal/wiki/Docker-QuickStart)

```bash
wget https://raw.githubusercontent.com/alibaba/canal/master/docker/run.sh 

sed -i 's/canal-server:latest/canal-server:1.1.5/g' run.sh

chmod +x run.sh

# 单 instance 模式？
sh run.sh -e canal.auto.scan=false \
		  -e canal.destinations=canal-instance-1 \
		  -e canal.instance.master.address=10.0.0.164:3306  \
		  -e canal.instance.dbUsername=canal  \
		  -e canal.instance.dbPassword=canal  \
		  -e canal.instance.connectionCharset=UTF-8 \
		  -e canal.instance.tsdb.enable=true \
		  -e canal.instance.gtidon=true \
		  -e canal.admin.manager=127.0.0.1:8089


# 集群模式？  只需基本的 canal-admin 的地址账号等参数，集群统一配置
sh run.sh -e canal.admin.manager=127.0.0.1:8089 \
         -e canal.admin.port=11110 \
         -e canal.admin.user=admin \
         -e canal.admin.passwd=4ACFE3202A5FF5CF467898FC58AAB1D615029441 \
         -e canal.admin.register.cluster=test
```


- [Refer to: Canal-Admin for docker docs](https://github.com/alibaba/canal/wiki/Canal-Admin-Docker)

```bash
wget https://raw.githubusercontent.com/alibaba/canal/master/docker/run_admin.sh

sed -i 's/canal-admin:latest/canal-admin:1.1.5/g' run_admin.sh

chmod +x run_admin.sh

# 指定外部的mysql作为canal-admin的库
sh  run_admin.sh -e server.port=8089 \
         -e spring.datasource.address=10.0.0.161 \
         -e spring.datasource.database=canaldb \
         -e spring.datasource.username=canal \
         -e spring.datasource.password=canal
```

Example Canal json:

```json
{"emptyCount":"111","binlogFileName":"binlog_xx","binlogFileOffset":123124,"dbName":"safecloud","tableName":"st_elecpower","columnValues":"addrIP,addrIPOrder,updateDate","crudType":"UPDATE","crudTime":1633778234000}
```

