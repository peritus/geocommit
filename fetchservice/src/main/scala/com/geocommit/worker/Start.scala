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

object Start {
    val properties = new Properties
    properties.load(new FileInputStream("config.properties"))

    val user = properties.getProperty("couchuser")
    val password = properties.getProperty("couchpass")

    val geocommitdb = new GeocommitDb(
        properties.getProperty("couchhost", "localhost"),
        properties.getProperty("couchport", "5894").toInt,
        if (user == null) None else Some((user, password))
    )

    def main(args: Array[String]) {
        val scanInit = new ScanInitWorker(geocommitdb)
        val scanUpdate = new ScanUpdateWorker(geocommitdb)

        scanInit.start
        scanUpdate.start
    }
}
