					; geocommit.com HTTP core
					; (c) 2010 David Soria Parra <dsp+geocommit@experimentalworks.net>
					;          Nils Adermann <naderman+geocommit@naderman.de>
					;          Filip Noetzel <filip+geocommit@j03.de>
					; Licensed under the terms of the MIT License
(ns #^{:doc "HTTP API core functions",
       :author "David Soria Parra"}
  geocommit.core
  (:use clojure.walk
	clojure.contrib.logging
	clojure.contrib.condition)
  (:require [clojure.contrib.str-utils2 :as s]
	    [clojure.contrib.trace :as t])
  (:import (java.text SimpleDateFormat)
	   (java.util Date TimeZone)))

(defrecord Commit [_id repository revision message author latitude longitude horizontal-accuracy vertical-accuracy source altitude direction type])

(defn- parse-geocommit-exp
  ([s k]
     (k {:long  (parse-geocommit-exp s #"(?s)geocommit \(1\.0\)\n(.*?)(?:\n\n|$)" #"\n" #":\s+")
	 :short (parse-geocommit-exp s #"geocommit\(1\.0\):\s(.*?);" #",\s+" #"\s+")}))
  ([s vers pairsep valsep]
     (if-let [st (re-find vers s)]
       (apply hash-map
	      (mapcat #(s/split % valsep)
		      (s/split (last st) pairsep))))))

(defn- tonumber [^String s]
  (if s
    (Double. s)))

(defn parse-geocommit
  "Parses a geocommit information and returns a geocommit structure
   including ident, author, hash and message."
  [ident hash author message geocommit]
  (let [{:keys [lat long hacc vacc src dir alt speed]}
	(keywordize-keys
	 (merge {"hacc" nil "vacc" nil "src" nil "dir" nil "speed" nil "alt" nil}
		(or (parse-geocommit-exp geocommit :short)
		    (parse-geocommit-exp geocommit :long))))]
    (if (not (or (nil? long) (nil? lat)))
      (Commit. (str "geocommit:" ident ":" hash)
	       ident hash message
	       author (tonumber lat) (tonumber long)
	       (tonumber hacc) (tonumber vacc)
	       src (tonumber alt) dir
	       "geocommit"))))

(defn isodate
  "Return a proper ISO 8601 formated date string"
  ([] (isodate (Date.)))
  ([date]
     (.format
      (doto
	  (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ssz") (.setTimeZone (TimeZone/getTimeZone "UTC")))
      date)))

(defmacro contains-all?
  "Like clojure.core/contains? but allows multiple keys.
   (contains-all? map :foo :bar) is equal to (and (contains? map :foo) (contains? map :bar))
   Allows nested queries like (contains-all? map [:foo :bar])."
  [val & keys]
  `(if (and ~@(map (fn [s] (if (vector? s)
			     `(-> ~val ~@s)
			     `(-> ~val ~s))) keys))
     true false))

(defmacro wrap
  [f & body]
  `(~f (fn [] ~@body)))

(defn error-handling
  [f]
  (handler-case :type
    (try
      (f)
      (catch IllegalArgumentException iae
	(raise :type :parse-error
	       :message (.getMessage iae)
	       :cause iae))
      (catch Exception e
	(raise :type :service-error
	       :message (.getMessage e)
	       :cause e)))
    (handle :parse-error
       (println (:message *condition*))
       (print-stack-trace *condition*)
      {:status 400})
    (handle :uri-error
      (println (:message *condition*))
      (print-stack-trace *condition*)
      {:status 400})
    (handle :service-error
      (println (:message *condition*))
      (print-stack-trace *condition*)
      {:status 500})))

#_(defmacro wrap-error-handling
  [& body]
  `(handler-case :type
     (try
       ~@body
       (catch IllegalArgumentException ~iae
	 (raise :type :parse-error))
       (catch Exception 'e
	 (raise :type :service-error)))
     (handle :parse-error
       (println (:message *condition*))
       (print-stack-trace *condition*)
       {:status 400})
     (handle :uri-error
       (println (:message *condition*))
       (print-stack-trace *condition*)
       {:status 400})
     (handle :service-error
       (println (:message *condition*))
       (print-stack-trace *condition*)
       {:status 500})))