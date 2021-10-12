#!/bin/bash

#/*
# * Copyright 2017 ~ 2025 the original author or authors. <Wanglsir@gmail.com, 983708408@qq.com>
# *
# * Licensed under the Apache License, Version 2.0 (the "License");
# * you may not use this file except in compliance with the License.
# * You may obtain a copy of the License at
# *
# *      http://www.apache.org/licenses/LICENSE-2.0
# *
# * Unless required by applicable law or agreed to in writing, software
# * distributed under the License is distributed on an "AS IS" BASIS,
# * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# * See the License for the specific language governing permissions and
# * limitations under the License.
# */

[ -z "$APP_HOME" ] && APP_HOME="/opt/apps/acm/eureka-server-package/eureka-server-master-bin"
[ -z "$BASE_DIR" ] && BASE_DIR="/mnt/disk1/eureka-server/"
[ -z "$LOG_DIR" ] && LOG_DIR="/mnt/disk1/log/eureka-server"

function start() {
  # Check already running
  if [ -n "$(getPids)" ]; then
    echo "Already running eureka server!"; exit 0
  fi
  local active=$1
  local execFile="${APP_HOME}/eureka-server-master-bin.jar"
  local jvmOpts="--server.tomcat.basedir=${BASE_DIR}"
  local logFile="$LOG_DIR/eureka-server.log"
  echo "Starting eureka server for active=\"$active\" ..."
  nohup java -jar ${execFile} ${jvmOpts} --spring.profiles.active=${active} --logging.file.name=${logFile} >/dev/null 2>&1 &
  echo "Starting eureka-server(peer1,peer2,peer3) completed! log writing to: ${logFile}"
}

function stop() {
  local active=$1
  # Check already running
  if [ -z "$(getPids)" ]; then
    echo "No running eureka server !"; exit 0
  fi
  echo "Stopping eureka server ..."
  ps -ef|grep java|grep -v grep|grep "$APP_HOME"|grep "$active"|cut -c 9-15|xargs kill -s TERM
  echo "Stopped eureka server successfully!"
}

function getPids() {
  local pids=$(ps -ef|grep java|grep -v grep|grep ${APP_HOME})
  echo $pids
}

# ----- Main call. -----
cmd=$1
arg1=$2
if [ -n "$arg1" ]; then
  active=$(echo "$arg1"|sed 's/--active=//g')
  if [ "$cmd" == "start" ]; then
    start "$active"
  elif [ "$cmd" == "stop" ]; then
    stop "$active"
  elif [ "$cmd" == "restart" ]; then
    stop "$active"
    start "$active"
  fi
  exit 0
fi
echo "Bad command args! Usage: {start --active=standalone|restart --active=ha,peer1,peer2,peer3|stop}"