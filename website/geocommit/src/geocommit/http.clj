					; geocommit.com HTTP request API
					; (c) 2010 David Soria Parra <dsp+geocommit@experimentalworks.net>
					;          Nils Adermann <naderman+geocommit@naderman.de>
					;          Filip Noetzel <filip+geocommit@j03.de>
					; Licensed under the terms of the MIT License
(ns #^{:doc "geocommit HTTP connections. Appengine compatible.",
       :author "David Soria Parra"}
  geocommit.http
  (:use clojure.contrib.json
	clojure.contrib.condition
	clojure.contrib.logging)
  (:require [clojure.contrib.trace :as t])
  (:import (java.net URL HttpURLConnection)
	   (java.io InputStreamReader BufferedReader PrintWriter BufferedWriter
		    OutputStreamWriter OutputStream InputStream)))

;; contrib mspit/mslurp as duck-stream use sockets which are not allowed on appengine
(defn- mspit [#^OutputStream f content]
  (with-open [#^PrintWriter w (PrintWriter. (BufferedWriter. (OutputStreamWriter. f "UTF-8")))]
    (.print w content)))

(defn- mslurp [#^InputStream f]
  (with-open [#^BufferedReader r (BufferedReader. (InputStreamReader. f "UTF-8"))]
    (let [sb (StringBuilder.)]
      (loop [c (.read r)]
	(if (neg? c)
	  (str sb)
	  (do (.append sb (char c))
	      (recur (.read r))))))))

(def *default-opts* {:method "GET"
		     :body nil
		     :content-type nil
		     :accept nil})
(def *params* {:accept "Accept"
	       :content-type "Content-Type"})

(defn http-req
  "Starts a synchronous http request using HttpURLConnection.
   Possible keys are
       :method \"POST\" or \"GET\"
       :accept e.g. \"application/json\"
       :content-type e.g. \"application/json\"
       :body The body"
  [url & options]
  (if url
    (let [conn (.openConnection (URL. url))
	  opts (merge *default-opts* (apply array-map options))]
      (doseq [[k v] opts]
	(if (and k (contains? *params* v))
	  (.setRequestProperty conn (*params* k) v)))
      (doto conn
	(.setRequestProperty "User-Agent" "geocommit http client")
	(.setRequestProperty "Connection" "Close")
	(.setRequestMethod (:method opts))
	(.setDoOutput true)
	(.setDoInput true)
	(.connect))
      (if (and (:body opts) (= "POST" (:method opts))); better check
	(mspit (.getOutputStream conn) (:body opts)))
      {:status (.getResponseCode conn)
       :body (mslurp (.getInputStream conn))})))

(defn http-call-service
  "Call a http service using http-req.
   Uses json as exchange format."
  ([service]
     (try
       (and service
	    (read-json
	     (:body
	      (http-req service))))
       (catch Exception e
	 (raise :type :service-error))))
  ([service body]
     (try
       (and service
	    body
	    (read-json
	     (:body
	      (http-req service
			:body (json-str body)
			:method "POST"
			:content-type "application/json"))))
       (catch Exception e
	 (raise :type :service-error
		:cause e)))))