import sbt._

class FetchServiceProject(info: ProjectInfo) extends ParentProject(info) {
    override def shouldCheckOutputDirectories = false

    lazy val web  = project(".", "Web Service", new WebServiceProject(info))
    lazy val worker = project(".",  "Worker", new ScanInitWorkerProject(info))

    val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"

    val jetty7 = "org.eclipse.jetty" % "jetty-webapp" % "7.0.2.v20100331" % "test"
    val servlet = "javax" % "javaee-web-api" % "6.0" % "provided"
    val beanstalk = "com.surftools" % "BeanstalkClient" % "1.4.4-SNAPSHOT"
    val liftjson = "net.liftweb" % "lift-json_2.8.0" % "2.1"
    val scouchdb = "scouch.db" % "scouchdb_2.8.0" % "0.6"

    class WebServiceProject(info: ProjectInfo) extends DefaultWebProject(info) {
      override val jettyPort = 8081
    }

    class ScanInitWorkerProject(info: ProjectInfo) extends DefaultProject(info) with ProguardProject{
        override def proguardOptions = List(
            proguardKeepMain("com.geocommit.worker.ScanInitWorker"))

    }

}
