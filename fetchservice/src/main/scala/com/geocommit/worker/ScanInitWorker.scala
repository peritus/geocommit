package com.geocommit.worker;

import com.geocommit.Git;

import com.surftools.BeanstalkClientImpl.ClientImpl
import scala.collection.immutable.HashMap
import net.liftweb.json.JsonParser
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonAST.{JValue, JObject, JField, JNull, JString}
import net.liftweb.json.JsonDSL._


object ScanInitWorker {
    val beanstalk = new ClientImpl("localhost", 11300)

    implicit def byteArray2String(b: Array[Byte]): String =
        new String(b, "UTF-8")

    def main(args: Array[String]) {
        while (true) {
            beanstalk watch "scan-init"

            val job = beanstalk reserve null

            println("Processing Job:")
            println(job.getData)

            if (JsonParser parse job.getData match {
                case json: JObject =>
                    process(json)
                case _ =>
                    false
            }) beanstalk delete job.getJobId
        }
    }

    def scanInit(repo: String, id: String): Boolean = {

        //if id.starts with github
            val git = new Git

            git.clone(repo)
            git.getGeocommitNotes(repo).map(Geocommit(_)).
                foreach(x => println(compact(JsonAST.render(x.toJson))))

        //else id.stats with bitbuckt
        true
    }

    def process(json: JObject): Boolean = {
        (json \ "repository-url") match {
            case JField(_, JString(repo)) =>
                (json \ "identifier") match {
                    case JField(_, JString(id)) =>
                        scanInit(repo, id)
                    case _ =>
                        false
                }
            case _ =>
                false
        }
    }

}
