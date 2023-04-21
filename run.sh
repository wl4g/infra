#!/bin/bash
# Copyright (c) 2017 ~ 2025, the original authors individual Inc,
# All rights reserved. Contact us James Wong <jameswong1376@gmail.com>
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
set -e

BASE_DIR="$(cd "`dirname $0`"; pwd)"
# If run.sh is a soft link, it is considered to be $PROJECT_HOME/run.sh, no need to call back the path.
if [ -L "`dirname $0`/run.sh" ]; then
  BASE_DIR="$(cd "`dirname $0`"; pwd)"
fi

DEFAULT_MAVEN_OPTS=${MAVEN_OPTS:-"-Xss64m -Xms1g -Xmx12g -XX:ReservedCodeCacheSize=1g -Dorg.slf4j.simpleLogger.defaultLogLevel=WARN"}
MAVEN_CLI_OPTS=${MAVEN_CLI_OPTS:-} # --no-transfer-progress

# eg1: log "error" "Failed to xxx"
# eg2: log "xxx complete!"
function log() {
  local logLevel=" \033[33mINFO\033[0m"
  local logContent=$1
  if [[ $# > 1 ]]; then
    logLevel=$1
    logContent=$2
  fi
  local logMsg="[$logLevel] $(date '+%Y-%m-%d %H:%M:%S') - $logContent"
  echo -e "$logMsg"
  echo -e "$logMsg" >> /tmp/run-$(basename $BASE_DIR).log
}

function logDebug() {
  log "\033[37mDEBUG\033[0m" "$@"
}

function logWarn() {
  log "\033[33mWARN \033[0m" "$@"
}

function logErr() {
  log "\033[31mERROR\033[0m" "$@"
}

function usages() {
    echo $"
Usage: ./$(basename $0) [OPTIONS] [arg1] [arg2] ...
    version                                         Print maven project POM version.
    upgrade-push                                    Upgrade pom and build push.
"
}

function print_pom_version() {
    # see:https://cloud.tencent.com/developer/article/1476991
    MAVEN_OPTS="$DEFAULT_MAVEN_OPTS -Dorg.slf4j.simpleLogger.log.org.apache.maven.plugins.help=INFO"
    #${BASE_DIR}/mvnw org.apache.maven.plugins:maven-help-plugin:3.3.0:evaluate -o -Dexpression=project.version | grep -v '[INFO]'
    POM_VERSION=$(${BASE_DIR}/mvnw -Dexec.executable='echo' -Dexec.args='${project.version}' --non-recursive exec:exec -q)
    unset MAVEN_OPTS
    echo $POM_VERSION
}

function do_upgrade_push() {
    new_version="$1"
    if [ -z "$new_version" ]; then
      logErr "<new version> is missing."; exit 1
    fi

    log "Upgrade set pom version to $new_version ..."
    $BASE_DIR/mvnw -f $BASE_DIR/pom.xml versions:set -DnewVersion=$new_version

    log "Cleaup the temporary pom version backup files ..."
    set +e
    git -C $BASE_DIR rm -rf pom.xml.versionsBackup >/dev/null 2>&1
    git -C $BASE_DIR rm -rf */pom.xml.versionsBackup >/dev/null 2>&1
    git -C $BASE_DIR rm -rf */*/pom.xml.versionsBackup >/dev/null 2>&1
    git -C $BASE_DIR rm -rf */*/*/pom.xml.versionsBackup >/dev/null 2>&1
    set -e

    git -C $BASE_DIR add .
    git -C $BASE_DIR commit -m "feat: upgrade to v$new_version"
    git -C $BASE_DIR tag v$new_version
    git -C $BASE_DIR push origin v$new_version
    git -C $BASE_DIR push
}

case $1 in
  version)
    print_pom_version
    ;;
  upgrade-push)
    do_upgrade_push "$2"
    ;;
  *)
    usages
    ;;
esac

