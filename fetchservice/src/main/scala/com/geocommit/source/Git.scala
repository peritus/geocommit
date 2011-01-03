package com.geocommit.source;

import com.geocommit.Geocommit;
import java.io.{File => JFile}
import scala.tools.nsc.io._
import scala.collection.mutable.StringBuilder;

class Git extends GeocommitSource {

    def lsremote(repo: String, ref: String): String = {
        println("git ls-remote \"" + repo + "\"")
        procOutput(startProc(List("git", "ls-remote", repo))).
            map(_ split "\\s+").find(_.last == ref) match {
            case Some(Array(sha1, ref)) =>
                sha1
            case None =>
                ""
        }
    }

    def clone(repo: String) {
        val repoDir = getRepoDir(repo)
        // foreach necessary to wait for termination
        if (new JFile(repoDir).exists()) {
          println("git fetch \"" + repo + "\" " + repoDir)
          cmdWait(List("git", "fetch"), repoDir)
        } else {
          println("git clone \"" + repo + "\" " + repoDir)
          cmdWait(List("git", "clone", repo, repoDir))
        }
        println("git fetch origin refs/notes/geocommit")
        Process("git fetch origin refs/notes/geocommit", cwd = repoDir).foreach(_ => null)
        println("git checkout refs/notes/geocommit")
        Process("git checkout refs/notes/geocommit", cwd = repoDir).foreach(_ => null)
        println("git merge FETCH_HEAD")
        Process("git merge FETCH_HEAD", cwd = repoDir).foreach(_ => null)
        println("git update-ref refs/notes/geocommit HEAD")
        Process("git update-ref refs/notes/geocommit HEAD", cwd = repoDir).foreach(_ => null)
    }

    def delete(repo: String) {
        val repoDir = getRepoDir(repo)
        println("rm " + repoDir)
        Process("rm " + repoDir)
    }

    def getGeocommits(repo: String, id: String): List[Geocommit] = {
        println("git notes --ref geocommit list cwd=" + getRepoDir(repo))
        parseNotes(
            repo, id,
            Process(
                "git notes --ref geocommit list", cwd = getRepoDir(repo)
            ).toList
        )
    }

    def getGeocommits(repo: String, id: String, commits: List[String]): List[Geocommit] = {
        val repoDir = getRepoDir(repo)
        parseNotes(
            repo, id,
            commits.map(
                (rev: String) => {
                    println("git notes --ref geocommit list " + rev)
                    val note = procOutput(
                        startProc(
                            List("git", "notes", "--ref", "geocommit", "list", rev),
                            repoDir
                        )
                    ).toList
                    if (note.length > 0) {
                        note.head + " " + rev
                    }
                    else {
                        ""
                    }
                }
            ).toList
        )
    }

    def parseNotes(repo: String, id: String, notesOutput: List[String]): List[Geocommit] = {
        notesOutput.map(
            _ split "\\s+"
        ).filter(
            _.length == 2
        ).map(
            (data: Array[String]) => {
                val noteObject = data.head
                val commit = data.last
                (
                    commit,
                    getCommit(commit, getRepoDir(repo)),
                    getObject(noteObject, getRepoDir(repo))
                )
            }
        ).filter{
            case (rev: String, (message: String, author: String), note: String) => {
                println(note)
                Geocommit.parsable(note) && (!message.isEmpty || !author.isEmpty)
            }
            case _ => false
        }.map{
            case (rev: String, (message: String, author: String), note: String) =>
                Geocommit(id, rev, message, author, note)
        }.toList
    }

    def getObject(obj: String, cwd: String): String = {
        val s = new StringBuilder
        println("git show " + obj)
        Process("git show " + obj, cwd = cwd).map(_ + "\n").addString(s)
        s toString
    }

    def getCommit(rev: String, cwd: String): (String, String) = {
        val subject = new StringBuilder
        println("git log -1 --pretty=\"format:%s\" " + rev + " --")
        Process("git log -1 --pretty=\"format:%s\" " + rev + " --", cwd=cwd).map(_ + "\n").addString(subject)

        val bodyBuilder = new StringBuilder
        println("git log -1 --pretty=\"format:%b\" " + rev + " --")
        Process("git log -1 --pretty=\"format:%b\" " + rev + " --", cwd=cwd).map(_ + "\n").addString(bodyBuilder)

        val body = bodyBuilder.toString()
        val bodyWithPrefix =
            if (body.isEmpty)
                ""
            else
                "\n\n" + body.toString()

        val message = subject.toString() + bodyWithPrefix

        val author = new StringBuilder
        println("git log -1 --pretty=\"format:%an <%ae>\" " + rev + " --")
        Process("git log -1 --pretty=\"format:%an <%ae>\" " + rev + " --", cwd=cwd).addString(author)

        (message, author toString)
    }
}
