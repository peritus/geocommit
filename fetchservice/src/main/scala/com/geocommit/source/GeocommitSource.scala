package com.geocommit.source;

import com.geocommit.Geocommit;
import java.security.MessageDigest;
import java.lang.{ProcessBuilder, Process};
import java.util.Arrays;
import java.io.{BufferedReader, InputStreamReader};

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

        bytes.map("%02x" format _ & 0xff).mkString
    }

    def startProc(cmd: List[String]): Process = {
        new ProcessBuilder(cmd).start()
    }

    def procOutput(proc: Process): Stream[String] = {
        val reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))
        Stream.continually(reader readLine)
    }

    def cmdWait(cmd: List[String]) = startProc(cmd).waitFor()

    def clone(repo: String);
    def delete(repo: String);
    def getGeocommits(repo: String, id: String): List[Geocommit];
}