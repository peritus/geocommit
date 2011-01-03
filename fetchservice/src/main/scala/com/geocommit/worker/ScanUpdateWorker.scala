package com.geocommit.worker;

import com.geocommit.source.GeocommitSource
import com.geocommit.source.Git
import com.geocommit.source.Bitbucket
import com.geocommit.GeocommitDb

import scala.collection.immutable.HashMap
import net.liftweb.json.JsonParser
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonAST.{JValue, JObject, JField, JArray, JString}
import net.liftweb.json.JsonDSL._
import java.util.Properties
import java.io.FileInputStream

class ScanUpdateWorker(
    geocommitdb: GeocommitDb
) extends Worker("scan-update", geocommitdb) {

    def scanUpdate(repo: String, id: String, commits: List[String]): Boolean = {
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
                source.getGeocommits(repo, id, commits).foreach(
                    x => geocommitdb.insertCommit(x)
                )
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
                        (json \ "commits") match {
                            case JField(_, JArray(commits)) =>
                                scanUpdate(repo, id, commits.map{
                                    case JString(rev) => rev
                                    case _ => ""
                                }.filter(_ != ""))
                            case _ =>
                                false
                        }
                    case _ =>
                        false
                }
            case _ =>
                false
        }
    }

}
