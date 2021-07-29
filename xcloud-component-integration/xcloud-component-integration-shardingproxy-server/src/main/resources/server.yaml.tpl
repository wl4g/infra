#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

######################################################################################################
# 
# If you want to configure governance, authorization and proxy properties, please refer to this file.
# 
######################################################################################################

# Governance configuration.
governance:
  name: shardingproxy
  overwrite: true
  registryCenter:
    type: ZooKeeper
    serverLists: localhost:2181
    props:
      retryIntervalMilliseconds: 500
      timeToLiveSeconds: 60
      maxRetries: 3
      operationTimeoutMilliseconds: 500

# Scaling configration.
#scaling:
#  blockQueueSize: 
#  workerThread: 

# Global rules configuration.
# @see:org.apache.shardingsphere.proxy.config.yaml.YamlProxyRuleConfiguration#rules
# @see:org.apache.shardingsphere.authority.yaml.config.YamlAuthorityRuleConfiguration
rules:
- !AUTHORITY
  users: # Note: it will cover the account number of the real database!!!
    - root@:123456
  provider:
    type: NATIVE
    #props:

# Global properties configuration.
props:
  max-connections-size-per-query: 1
  acceptor-size: 16  # The default value is available processors count * 2.
  executor-size: 16  # Infinite by default.
  proxy-frontend-flush-threshold: 128  # The default value is 128.
    # LOCAL: Proxy will run with LOCAL transaction.
    # XA: Proxy will run with XA transaction.
    # BASE: Proxy will run with B.A.S.E transaction.
  proxy-transaction-type: LOCAL
  proxy-opentracing-enabled: false
  proxy-hint-enabled: false
  query-with-cipher-column: true
  sql-show: true
  check-table-metadata-enabled: false
  failover-enable: true # Default by true
  failover-inspectInitialDelayMs: 3_000
  failover-inspectMinDelayMs: 3_000
  failover-inspectMaxDelayMs: 10_000
  # Failover admin dataSource configuration.
  # Notes: This configuration is used for read-write separation data source failover. Therefore, the same account
  #   password must be created for all master and slave databases before service startup.
  failover-admin-datasource-configuration-json: |-
      [{
          "schemaName": "userdb",
          "username": "root",
          "password": "123456",
          "backendDbNodeAddressMapping": [{
              "internalNodeAddress": "wanglsir-pro:13306",
              "externalMappingAddresses": [
                  "wl4g.debug:13306"
              ]
          },{
              "internalNodeAddress": "wanglsir-pro:13307",
              "externalMappingAddresses": [
                  "wl4g.debug:13307"
              ]
          },{
              "internalNodeAddress": "wanglsir-pro:13308",
              "externalMappingAddresses": [
                  "wl4g.debug:13308"
              ]
          }]
      }]
  # Notes: If failover is enabled and distributed governance mode is adopted, lock must be opened.
  lock-enabled: true # Default by false
