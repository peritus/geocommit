package com.geocommit

import com.surftools.BeanstalkClientImpl.ClientImpl
import scala.collection.immutable.HashMap

import sjson.json._
import scala.reflect._


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

    @JSONProperty("horizontal-accuracy")
    @OptionTypeHint(classOf[Double])
    val horizontalAccuracy: Option[Double],

    @JSONProperty("vertical-accuracy")
    @OptionTypeHint(classOf[Double])
    val verticalAccuracy: Option[Double]
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
                f => data.startsWith(f.getPrefix())
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
}
