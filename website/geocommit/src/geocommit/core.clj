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
(def *geocommit-short-regex*
     #"geocommit(\(\d+\.\d+\)): (.+,\s)(long|lat|acc|src) ([\d\w\.]+);")
(def *geocommit-long-regex*
     #"geocommit (\(\d+\.\d+\))\n")
(def *geocommit-subex*
     #"(long|lat|hacc|src) ([\d\w\.]+),")

(defstruct commit :_id :identifier :commit :message :author :latitude :longitude :horizontal-accurancy :source :type)

(defn- number [n]
  (Float/valueOf n))

(defn- parse-geocommit-short [message]
  "Parse a short geocommit format 1.0.
   Supports long, lat, acc, src."
  (when-let [[_ version options k v]
	     (re-find *geocommit-short-regex* message)]
    (merge
     (apply merge (map #(hash-map (get %1 1) (get %1 2))
		       (re-seq *geocommit-subex* options)))
     {k v})))

(defn- parse-geocommit-long 
  "Parse a long geocommit format 1.0
   Supports long, lat, acc, src."
  [message]
  (when-let [found
	     (second (s/split message #"geocommit (\(\d+\.\d+\))\n"))]
    (apply merge (map #(hash-map (get %1 1) (get %1 2))
		      (re-seq #"(long|lat|hacc|src): ([^\;\,]+?)\n"
			      found)))))

(defn parse-geocommit
  "Parses a geocommit information and returns a geocommit structure
   including ident, author, hash and message."
  [ident hash author message geocommit]
  (let [{lat "lat" long "long" hacc "hacc" src "src"}
	(merge
	 {"hacc" "0" "src" ""}
	 (or (parse-geocommit-short geocommit)
	     (parse-geocommit-long geocommit)))]
    (if (not (or (nil? long) (nil? lat)))
      (struct commit (str "geocommit:" ident ":" hash) ident hash message author (number lat) (number long) (number hacc) src "geocommit"))))

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