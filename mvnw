#!/bin/sh
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

# MAVEN_CMD_LINE_ARGS is expanded during preprocessing, so can contain spaces.
PRG="$0"
DIRNAME=`dirname "$PRG"`
DIRNAME=`cd "$DIRNAME" && pwd`
MAVEN_CMD_LINE_ARGS="$@"
M2_HOME=`cd "$DIRNAME/.." && pwd`

JAVA_HOME=$JAVA_HOME
if [ "$JAVA_HOME" = "" ]; then
    echo "Error: JAVA_HOME is not set"
    exit 1
fi

JAVA_EXE="$JAVA_HOME/bin/java"
exec "$JAVA_EXE" \
    -cp ".mvn/wrapper/maven-wrapper.jar" \
    "org.apache.maven.wrapper.MavenWrapperMain" \
    "$MAVEN_CMD_LINE_ARGS"



