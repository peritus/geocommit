package com.geocommit.source;

import com.geocommit.Geocommit;
import java.security.MessageDigest;
import java.lang.{ProcessBuilder, Process};
import java.util.Arrays;
import java.io.{BufferedReader, InputStreamReader, File};
import scala.io.Source;

abstract class GeocommitSource {
    implicit def convertScalaListToJavaList(aList:List[String]) =
        java.util.Arrays.asList(aList.toArray: _*)
    implicit def string2ByteArray(s: String): Array[Byte] =
        s.getBytes("UTF-8")
    implicit def byteArray2String(b: Array[Byte]): String =
        new String(b, "UTF-8")

    def getRepoDir(repo: String): String = {
        val md = MessageDigest getInstance "SHA"
        val bytes = md digest repo

        "../repositories/" + bytes.map("%02x" format _ & 0xff).mkString
    }

    def startProc(cmd: List[String], cwd: String = "."): Process = {
        val pb = new ProcessBuilder(cmd)
        pb.directory(new File(cwd))
        pb.start()
    }

    def procOutput(proc: Process): Iterator[String] = {
        Source.fromInputStream(proc.getInputStream()).getLines()
    }

    def cmdWait(cmd: List[String], cwd: String = ".") = startProc(cmd, cwd).waitFor()

    def clone(repo: String);
    def delete(repo: String);
    def getGeocommits(repo: String, id: String): List[Geocommit];
    def getGeocommits(repo: String, id: String, commits: List[String]): List[Geocommit];
}