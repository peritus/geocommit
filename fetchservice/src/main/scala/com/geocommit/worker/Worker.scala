package com.geocommit.worker;

import com.geocommit.source.GeocommitSource
import com.geocommit.source.Git
import com.geocommit.source.Bitbucket
import com.geocommit.GeocommitDb

import scala.actors.Actor
import com.surftools.BeanstalkClientImpl.ClientImpl
import scala.collection.immutable.HashMap
import net.liftweb.json.JsonParser
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonAST.{JValue, JObject, JField, JNull, JString}
import net.liftweb.json.JsonDSL._
import java.util.Properties
import java.io.FileInputStream

abstract class Worker(
    val tube: String,
    val geocommitdb: GeocommitDb
) extends Actor {
    val beanstalk = new ClientImpl("localhost", 11300)

    implicit def byteArray2String(b: Array[Byte]): String =
        new String(b, "UTF-8")

    def act() {
        while (true) {
            beanstalk watch tube

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
                        + " for 120 seconds\n\nStack trace:\n")
                    e.printStackTrace
                    beanstalk.release(job.getJobId, 200, 120)
            }
        }
    }


    def process(json: JObject): Boolean;
}
