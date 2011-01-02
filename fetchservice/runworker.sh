#!/bin/sh
LIBS="target/scala_2.8.0/classes/"
LIBS="${LIBS}:lib_managed/scala_2.8.0/compile/BeanstalkClient-1.4.4-SNAPSHOT.jar"
LIBS="${LIBS}:lib_managed/scala_2.8.0/compile/commons-codec-1.3.jar"
LIBS="${LIBS}:lib_managed/scala_2.8.0/compile/commons-logging-1.1.1.jar"
LIBS="${LIBS}:lib_managed/scala_2.8.0/compile/dispatch-futures_2.8.0-0.7.4.jar"
LIBS="${LIBS}:lib_managed/scala_2.8.0/compile/dispatch-http_2.8.0-0.7.4.jar"
LIBS="${LIBS}:lib_managed/scala_2.8.0/compile/dispatch-http-json_2.8.0-0.7.4.jar"
LIBS="${LIBS}:lib_managed/scala_2.8.0/compile/dispatch-json_2.8.0-0.7.4.jar"
LIBS="${LIBS}:lib_managed/scala_2.8.0/compile/httpclient-4.0.1.jar"
LIBS="${LIBS}:lib_managed/scala_2.8.0/compile/httpcore-4.0.1.jar"
LIBS="${LIBS}:lib_managed/scala_2.8.0/compile/junit-4.8.1.jar"
LIBS="${LIBS}:lib_managed/scala_2.8.0/compile/lift-json_2.8.0-2.1.jar"
LIBS="${LIBS}:lib_managed/scala_2.8.0/compile/paranamer-2.0.jar"
LIBS="${LIBS}:lib_managed/scala_2.8.0/compile/scouchdb_2.8.0-0.6.jar"
LIBS="${LIBS}:lib_managed/scala_2.8.0/compile/sjson-0.8.jar"

LIBS="${LIBS}:project/boot/scala-2.8.0/lib/scala-library.jar"
LIBS="${LIBS}:project/boot/scala-2.8.0/lib/scala-compiler.jar"

echo $LIBS
export JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
scala -version
#jswat -classpath "$LIBS" com.geocommit.worker.ScanInitWorker
#scala -classpath "$LIBS" com.geocommit.TestCouch
scala -classpath "$LIBS" com.geocommit.worker.Start
