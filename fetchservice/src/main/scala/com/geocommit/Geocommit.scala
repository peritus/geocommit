package com.geocommit

import com.surftools.BeanstalkClientImpl.ClientImpl
import scala.collection.immutable.HashMap

import sjson.json._
import scala.reflect._
import scala.annotation.target._
import scala.util.matching.Regex

abstract class GeocommitFormat {
    def getPrefix(): String
    def getEndMarker(): String
    def getElementSeparator(): String
    def getKeyValueSeparator(): String
    def getRegex(): String
    def getPrefixLength(): Int = getPrefix().length()

    def getMap(data: String): Map[String, String] = {
        val r = new Regex(getRegex())
        val matches = r.findAllIn(data).toList

        Map(matches.last.substring(
                getPrefixLength,
                matches.last.indexOf(getEndMarker) match {
                    case x if x < 0 => matches.last.length() - 1
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

    def parsable(data: String): Boolean = {
        val r = new Regex(getRegex())
        r.findFirstIn(data) match {
            case Some(m) => true
            case None => false
        }
    }
}

object GeocommitShortFormat extends GeocommitFormat {
    override def getPrefix(): String = "geocommit(1.0): "
    override def getEndMarker(): String = ";"
    override def getElementSeparator(): String = ",\\s*"
    override def getKeyValueSeparator(): String = "\\s+"
    override def getRegex(): String = "(geocommit\\(1\\.0\\):[^;]+;)"
}

object GeocommitLongFormat extends GeocommitFormat {
    override def getPrefix(): String = "geocommit (1.0)\n"
    override def getEndMarker(): String = "\n\n"
    override def getElementSeparator(): String = "\\s*\n\\s*"
    override def getKeyValueSeparator(): String = ":\\s*"
    override def getRegex(): String = "(geocommit \\(1\\.0\\)\n(?:[^;]+\n)+(?:\n|$))"
}

@BeanInfo
case class Geocommit(
    val repository: String,
    val revision: String,
    val message: String,
    val author: String,
    val latitude: Double,
    val longitude: Double,
    val source: String,

    @OptionTypeHint(classOf[Double])
    val altitude: Option[Double],

    @OptionTypeHint(classOf[Double])
    val speed: Option[Double],

    @OptionTypeHint(classOf[Double])
    val direction: Option[Double],

    @(JSONProperty @getter)(value = "horizontal-accuracy")
    @OptionTypeHint(classOf[Double])
    val horizontalAccuracy: Option[Double],

    @(JSONProperty @getter)(value = "vertical-accuracy")
    @OptionTypeHint(classOf[Double])
    val verticalAccuracy: Option[Double],

    @(JSONProperty @getter)(value = "type")
    val docType: String = "geocommit"
)

object Geocommit {
    def apply(
        repository: String,
        revision: String,
        message: String,
        author: String,
        data: String
    ) = {
        val values = List(
                GeocommitLongFormat, GeocommitShortFormat
            ).filter(
                f => f.parsable(data)
            ).head.getMap(data)

        new Geocommit(
            repository,
            revision,
            message,
            author,
            values("lat").toDouble,
            values("long").toDouble,
            values("src"),
            values.get("alt").map(_.toDouble), // None -> None, Option[String] -> Some(Double(s))
            values.get("speed").map(_.toDouble),
            values.get("dir").map(_.toDouble),
            values.get("hacc").map(_.toDouble),
            values.get("vacc").map(_.toDouble)
        )
    }

    def parsable(data: String): Boolean = {
        List(
            GeocommitLongFormat, GeocommitShortFormat
        ).filter(
            f => f.parsable(data)
        ).length > 0
    }
}
