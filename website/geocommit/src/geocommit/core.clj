					; geocommit.com HTTP API core
					; (c) 2010 The Geocommit Project
					; (c) 2010 David Soria Parra
					; Licensed under the terms of the MIT License
(ns #^{:doc "HTTP API core functions",
       :author "David Soria Parra"}
  geocommit.core
  (:use clojure.walk)
  (:require [clojure.contrib.str-utils2 :as s]
	    [clojure.contrib.trace :as t])
  (:import (java.text SimpleDateFormat)
	   (java.util Date TimeZone)))

(defrecord Commit [_id identifier commit message author latitude longitude horizontal-accurancy source type])

(defn- parse-geocommit-exp
  ([s k]
     (k {:long  (parse-geocommit-exp s #"(?s)geocommit \(1\.0\)\n(.*?)(?:\n\n|$)" #"\n" #":\s+")
	 :short (parse-geocommit-exp s #"geocommit\(1\.0\):\s(.*?);" #",\s+" #"\s+")}))
  ([s vers pairsep valsep]
     (if-let [st (re-find vers s)]
       (apply hash-map
	      (mapcat #(s/split % valsep)
		      (s/split (last st) pairsep))))))

(defn parse-geocommit
  "Parses a geocommit information and returns a geocommit structure
   including ident, author, hash and message."
  [ident hash author message geocommit]
  (let [{lat "lat" long "long" hacc "hacc" src "src"}
	(merge {"hacc" "0" "src" ""}
	       (or (parse-geocommit-exp geocommit :short)
		   (parse-geocommit-exp geocommit :long)))]
    (if (not (or (nil? long) (nil? lat)))
      (Commit. (str "geocommit:" ident ":" hash)
	       ident hash message
	       author (Double. lat) (Double. long)
	       (Double. hacc) src "geocommit"))))

(defn isodate
  "Return a proper ISO 8601 formated date string"
  ([] (isodate (Date.)))
  ([date]
     (.format
      (doto
	  (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ssz") (.setTimeZone (TimeZone/getTimeZone "UTC")))
      date)))

(defn contains-all?
  "Like clojure.core/contains? but allows multiple keys.
   (contains-all? map :foo :bar) is equal to (and (contains? map :foo) (contains? map :bar))"
  [val & keys]
  (every? #(= true %)
	  (map #(contains? val %) keys)))