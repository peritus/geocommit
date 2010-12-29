package com.geocommit.worker;

import com.geocommit.source.GeocommitSource;
import com.geocommit.source.Git;
import com.geocommit.source.Bitbucket;
import com.geocommit.GeocommitDb;

import com.surftools.BeanstalkClientImpl.ClientImpl
import scala.collection.immutable.HashMap
import net.liftweb.json.JsonParser
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonAST.{JValue, JObject, JField, JNull, JString}
import net.liftweb.json.JsonDSL._

object ScanInitWorker {
    val beanstalk = new ClientImpl("localhost", 11300)
    val couchdb = new GeocommitDb("127.0.0.1")

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
        (if (id.startsWith("github")) {
            Some(new Git)
        }
        else if (id.startsWith("bitbucket")) {
            Some(new Bitbucket)
        }
        else {
            None
        }) match {
            case Some(source) =>
                source.clone(repo)
                source.getGeocommits(repo, id).foreach(
                    x => couchdb.insertCommit(x)
                )
                //source.delete(repo)
                couchdb.repoSetScanned(id)
                true
            case _ =>
                false
        }
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
