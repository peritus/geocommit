package com.geocommit.worker;

import com.geocommit.source.GeocommitSource
import com.geocommit.source.Git
import com.geocommit.source.Bitbucket
import com.geocommit.GeocommitDb

import com.surftools.BeanstalkClientImpl.ClientImpl
import scala.collection.immutable.HashMap
import net.liftweb.json.JsonParser
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonAST.{JValue, JObject, JField, JNull, JString}
import net.liftweb.json.JsonDSL._
import java.util.Properties
import java.io.FileInputStream

object ScanInitWorker {
    val beanstalk = new ClientImpl("localhost", 11300)

    val properties = new Properties
    properties.load(new FileInputStream("config.properties"))

    val user = properties.getProperty("couchuser")
    val password = properties.getProperty("couchpass")

    val couchdb = new GeocommitDb(
        properties.getProperty("couchhost", "localhost"),
        properties.getProperty("couchport", "5894").toInt,
        if (user == null) None else Some((user, password))
    )

    implicit def byteArray2String(b: Array[Byte]): String =
        new String(b, "UTF-8")

    def main(args: Array[String]) {
        while (true) {
            beanstalk watch "scan-init"

            val job = beanstalk reserve null

            println("Processing Job:")
            println(job.getData)

            try {
                if (JsonParser parse job.getData match {
                    case json: JObject =>
                        process(json)
                    case _ =>
                       false
                }) beanstalk delete job.getJobId
            } catch {
                case e: Exception =>
                    println("Failed with exception: " + e.getMessage
                        + "\ndelay job " + job.getJobId.toString
                        + " for 120 seconds")
                    beanstalk.release(job.getJobId, 200, 120)
            }
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
