package com.geocommit.source;

import com.geocommit.Geocommit
import scala.tools.nsc.io._
import scala.collection.mutable.StringBuilder

class Bitbucket extends GeocommitSource {
    def clone(repo: String) {
        val repoDir = getRepoDir(repo)
        // foreach necessary to wait for termination
        println("hg clone \"" + repo + "\" " + repoDir)
        cmdWait(List("hg", "clone", repo, repoDir))
    }

    def delete(repo: String) {
        val repoDir = getRepoDir(repo)
        println("rm " + repoDir)
        Process("rm " + repoDir)
    }

    def getGeocommits(repo: String, id: String): List[Geocommit] = {
        val repoDir = getRepoDir(repo)

        // truly random, banged head on keyboard
        val sep1 = "ghhugrij5oijtu3099324knmvvfu0g9u34fd09ufs"
        val sep2 = "dfggddgdfgijorejgehui43n0ßt4t9t4ugt4ggre"

        val output = new StringBuilder;

        println("hg log --template \"{node}" + sep1 + "{author}" + sep1 + "{desc}" + sep2 + "\"")
        Process(
                "hg log --template \"{node}" + sep1 + "{author}" + sep1 + "{desc}" + sep2 + "\"",
                cwd = repoDir
            ).map(_ + "\n").addString(output)

        output.toString.split(sep2).map(
                _ split sep1
            ).filter(
                _.length == 3
            ).map(
                (data: Array[String]) => {
                    if (Geocommit.parsable(data(2))) {
                        Geocommit(id, data(0), data(2), data(1), data(2))
                    }
                    else {
                        null
                    }
                }
            ).toList.filter{
                case null => false
                case _ => true
            }
    }

    def getGeocommits(repo: String, id: String, commits: List[String]): List[Geocommit] = {
        List()
    }
}