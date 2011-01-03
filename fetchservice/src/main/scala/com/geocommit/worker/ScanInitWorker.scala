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

class ScanInitWorker(
    geocommitdb: GeocommitDb
) extends Worker("scan-init", geocommitdb) {

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
                    x => geocommitdb.insertCommit(x)
                )
                //source.delete(repo)
                geocommitdb.repoSetScanned(id)
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
