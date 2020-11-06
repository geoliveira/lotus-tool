#!/bin/bash 

mvn clean install
jar xf lotus-zip/target/lotus-tool.zip
mv lotus-tool/ lotus-zip/target/
java -jar lotus-zip/target/lotus-tool/lotus-app-3.0-alpha-SNAPSHOT-jfx.jar
