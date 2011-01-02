package com.geocommit

import scouch.db._
import dispatch.Http
import sjson.json._
import scala.reflect._
import scala.annotation.target._
import java.lang.Thread

@BeanInfo
case class Repository(
    val identifier: String,
    val name: String,
    @(JSONProperty @getter)(ignoreIfNull = true)
    val description: String,

    @(JSONProperty @getter)(ignoreIfNull = true, value = "repository-url")
    val repositoryUrl: String,
    val vcs: String,
    var scanned: Boolean,
    @(JSONProperty @getter)(value = "type")
    val docType: String
) {
   private def this() = this(null, null, null, null, null, false, null)
}

class GeocommitDb(val host: String, val port: Int, val auth: Option[(String, String)]) {
    val http = new Http
    val db = Db(Couch(host, port, auth), "geocommit")

    def insertCommit(commit: Geocommit) {
        val id = "geocommit:" + commit.repository + ":" + commit.revision
        val doc = Doc(db, id)

        println("Adding geocommit")
        try {
            http(doc add commit)
        }
        catch {
            case e: dispatch.StatusCode =>
                if (e.code == 409)
                    println("Document already exists")
                else
                    throw e
        }
        println("Done adding geocommit")
    }

    def repoSetScanned(repo: String) {
        val id = "repository:" + repo

        println("update repository in couchdb")
        http(db.get[Repository](id)) match {
            case (id: String, rev: String, repo: Repository) =>
                repo.scanned = true

                val doc = Doc(db, id)
                http(doc update(repo, rev))
        }
    }
}
