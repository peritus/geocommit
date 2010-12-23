package com.geocommit;

import scala.tools.nsc.io._
import java.security.MessageDigest;
import scala.collection.mutable.StringBuilder;

class Git {
    implicit def string2ByteArray(s: String): Array[Byte] =
        s.getBytes("UTF-8")
    implicit def byteArray2String(b: Array[Byte]): String =
        new String(b, "UTF-8")

    def lsremote(repo: String, ref: String): String = {
        Process("git ls-remote " + repo).
            map(_ split "\\s+").find(_.last == ref) match {
            case Some(Array(sha1, ref)) =>
                sha1
            case None =>
                ""
        }
    }

    def getRepoDir(repo: String): String = {
        val md = MessageDigest getInstance "SHA"
        val bytes = md digest repo

        bytes.map("%02x" format _ & 0xff).mkString
    }

    def clone(repo: String) {
        val repoDir = getRepoDir(repo)
        Process("git clone " + repo + " " + repoDir).foreach(_ => null)
        Process("git fetch origin refs/notes/geocommit", cwd = repoDir).foreach(_ => null)
        Process("git checkout refs/notes/geocommit", cwd = repoDir).foreach(_ => null)
        Process("git merge FETCH_HEAD", cwd = repoDir).foreach(_ => null)
        Process("git update-ref refs/notes/geocommit HEAD", cwd = repoDir).foreach(_ => null)
    }

    def delete(repo: String) {
        val repoDir = getRepoDir(repo)
        Process("rm " + repoDir)
    }

    def getGeocommitNotes(repo: String): Boolean = {
        println("git notes --ref geocommit list " + getRepoDir(repo))
        Process("git notes --ref geocommit list", cwd = getRepoDir(repo)).
            map(_ split "\\s+").foreach(
            (data: Array[String]) => {
                val noteObject = data.head
                val commit = data.last

                getObject(noteObject, getRepoDir(repo))
            }
        )
    }

    def getObject(obj: String, cwd: String): String = {
        val s = new StringBuilder
        Process("git show " + obj, cwd = cwd) addString(s + "\n")
        s toString
    }
}