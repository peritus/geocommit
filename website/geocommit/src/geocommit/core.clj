					; geocommit bitbucket POST hook
					; (c) 2010 the geocommit project
					; (c) 2010 David Soria Parra
					; Licensed under the terms of the MIT License
(ns geocommit.core
  (:use clojure.walk)
  (:require [clojure.contrib.str-utils2 :as s]
	    [clojure.contrib.trace :as t])
  (:import (java.text SimpleDateFormat)
	   (java.util Date TimeZone)))
(defn- number [n]
  (Float/valueOf n))

(def *geocommit-short-regex*
     #"geocommit(\(\d+\.\d+\)): (.+,\s)(long|lat|acc|src) ([\d\w\.]+);")
(def *geocommit-long-regex*
     #"geocommit (\(\d+\.\d+\))\n")
(def *geocommit-subex*
     #"(long|lat|hacc|src) ([\d\w\.]+),")

(defstruct commit :identifier :commit :message :author :latitude :longitude :horizontal-accurancy :source :type)

(defn isodate
  ([] (isodate (Date.)))
  ([date]
     (.format
      (doto
	  (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ssz") (.setTimeZone (TimeZone/getTimeZone "UTC")))
      date)))

(defn- parse-geocommit-short [message]
  (when-let [[_ version options k v]
	     (re-find *geocommit-short-regex* message)]
    (merge
     (apply merge (map #(hash-map (get %1 1) (get %1 2))
		       (re-seq *geocommit-subex* options)))
     {k v})))

(defn- parse-geocommit-long [message]
  (when-let [found
	     (second (s/split message #"geocommit (\(\d+\.\d+\))\n"))]
    (apply merge (map #(hash-map (get %1 1) (get %1 2))
		      (re-seq #"(long|lat|hacc|src): ([^\;\,]+?)\n"
			      found)))))

(defn parse-geocommit [ident hash author message geocommit]
  (let [{lat "lat" long "long" hacc "hacc" src "src"}
	(merge
	 (or (parse-geocommit-short geocommit)
	     (parse-geocommit-long geocommit))
	 {"hacc" "0" "src" ""})]
    (if (not (or (nil? long) (nil? lat)))
      (struct commit ident hash message author (number lat) (number long) (number hacc) src "geocommit"))))