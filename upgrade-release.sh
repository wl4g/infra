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

new_version="$1"
if [ -z "$new_version" ]; then
  echo "<new version> is missing."; exit 1
fi

echo "Updating pom version to $new_version ..."
$BASE_DIR/mvnw versions:set -DnewVersion=$new_version

cd $BASE_DIR
git add .

echo "Cleanup new temporary pom.version backup files ..."
set +e
git rm -rf pom.xml.versionsBackup >/dev/null 2>&1
git rm -rf */pom.xml.versionsBackup >/dev/null 2>&1
git rm -rf */*/pom.xml.versionsBackup >/dev/null 2>&1
git rm -rf */*/*/pom.xml.versionsBackup >/dev/null 2>&1
set -e

git add .
git commit -m "feat: upgrade to v$new_version"
git tag v$new_version
git push origin v$new_version
git push

