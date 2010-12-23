#!/bin/sh
scala -classpath target/scala_2.8.0/classes/:lib_managed/scala_2.8.0/compile/BeanstalkClient-1.4.4-SNAPSHOT.jar:lib_managed/scala_2.8.0/compile/lift-json_2.8.0-2.1.jar com.geocommit.worker.ScanInitWorker
