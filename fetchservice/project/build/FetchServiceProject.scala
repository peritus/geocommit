import sbt._

class FetchServiceProject(info: ProjectInfo) extends DefaultWebProject(info) {
    val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"

    val jetty7 = "org.eclipse.jetty" % "jetty-webapp" % "7.0.2.v20100331" % "test"
    val servlet = "javax" % "javaee-web-api" % "6.0" % "provided"
    val beanstalk = "com.surftools" % "BeanstalkClient" % "1.4.4-SNAPSHOT"
    val liftjson = "net.liftweb" % "lift-json_2.8.0" % "2.1"
    val scouchdb = "scouch.db" % "scouchdb_2.8.0" % "0.6"
}
