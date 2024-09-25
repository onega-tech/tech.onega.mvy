#!/bin/bash

CUR_DIR=`pwd`
PROJECT_DIR=`readlink -f "$CUR_DIR/../../../../"`
JAR="$PROJECT_DIR/target/mvy.jar"

  . jdk17g

function build(){
  cd $PROJECT_DIR
  pwd
  mvn clean package
  cd $CUR_DIR
}

function testEmpty(){
  cd $CUR_DIR
  java -jar $JAR
}

function testHelp(){
  cd $CUR_DIR
  java -jar $JAR help
}

function testVersion(){
  cd $CUR_DIR
  java -jar $JAR version
}

function testGen(){
  cd $CUR_DIR
  java -jar $JAR gen . pom2.xml
}

function testMvnVersion(){
  cd $PROJECT_DIR
  export MAVEN_OPTS="-Xms32m"
  java -jar $JAR mvn -version
}


function testMvnBuild(){
  cd $PROJECT_DIR
  java -jar $JAR mvn clean package
}


$1


