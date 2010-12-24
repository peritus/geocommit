package com.geocommit.worker;

import com.geocommit.Git;

import com.surftools.BeanstalkClientImpl.ClientImpl
import scala.collection.immutable.HashMap
import net.liftweb.json.JsonParser
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonAST.{JValue, JObject, JField, JNull, JString}
import net.liftweb.json.JsonDSL._


abstract class GeocommitFormat {
    def getPrefix(): String
    def getEndMarker(): String
    def getElementSeparator(): String
    def getKeyValueSeparator(): String
    def getPrefixLength(): Int = getPrefix().length()

    def getMap(data: String): Map[String, String] = {
        Map(data.substring(
                getPrefixLength,
                data indexOf getEndMarker match {
                    case x if x < 0 => data.length() - 1
                    case n => n
                }
            ).split(
                getElementSeparator
            ).map(
                s => s split getKeyValueSeparator
            ).filter(
                _.length == 2
            ).map(
                a => (a.head, a.last)
            )
            : _*
        )
    }
}

object GeocommitShortFormat extends GeocommitFormat {
    override def getPrefix(): String = "geocommit(1.0): "
    override def getEndMarker(): String = ";"
    override def getElementSeparator(): String = ",\\s*"
    override def getKeyValueSeparator(): String = "\\s+"
}

object GeocommitLongFormat extends GeocommitFormat {
    override def getPrefix(): String = "geocommit (1.0)\n"
    override def getEndMarker(): String = "\n\n"
    override def getElementSeparator(): String = "\\s*\n\\s*"
    override def getKeyValueSeparator(): String = ":\\s*"
}


class Geocommit(
    lat: Double,
    long: Double,
    src: String,
    alt: Option[Double],
    speed: Option[Double],
    dir: Option[Double],
    hacc: Option[Double],
    vacc: Option[Double]
) {
    def toJson(): JObject = {
        val obj =
            ("lat" -> lat) ~
            ("long" -> long) ~
            ("src" -> src)

        obj ~ List(
            ("alt", alt),
            ("speed", speed),
            ("dir", dir),
            ("hacc", hacc),
            ("vacc", vacc)
        ).filter{
            case (_, Some(_)) => true
            case (_, None) => false
        }
        .map{
            case (key, value) => JField(key, value)
        }
    }
}

object Geocommit {
    def apply(
        lat: Double,
        long: Double,
        src: String,
        alt: Option[Double],
        speed: Option[Double],
        dir: Option[Double],
        hacc: Option[Double],
        vacc: Option[Double]
    ) = new Geocommit(lat, long, src, alt, speed, dir, hacc, vacc)

    def apply(data: String) = {
        val values = List(
                GeocommitLongFormat, GeocommitShortFormat
            ).filter(
                f => data.startsWith(f.getPrefix())
            ).head.getMap(data)

        new Geocommit(
            values("lat").toDouble,
            values("long").toDouble,
            values("src"),
            values.get("alt").map(_.toDouble),
            values.get("speed").map(_.toDouble),
            values.get("dir").map(_.toDouble),
            values.get("hacc").map(_.toDouble),
            values.get("vacc").map(_.toDouble)
        )
    }
}