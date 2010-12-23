(ns #^{:doc "geocommit HTTP connections. Appengine compatible.",
       :author "David Soria Parra"}
  geocommit.http
  (:use clojure.contrib.json
	clojure.contrib.condition)
  (:require [clojure.contrib.trace :as t]) 
  (:import (java.net URL)
	   (java.io InputStreamReader BufferedReader PrintWriter BufferedWriter OutputStreamWriter)))

;; contrib mspit/mslurp as duck-stream use sockets which are not allowed on appengine
(defn- mspit [f content]
  (with-open [#^PrintWriter w (PrintWriter. (BufferedWriter. (OutputStreamWriter. f "UTF-8")))]
    (.print w content)))

(defn- mslurp [f]
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
  (let [conn (.openConnection (URL. url))
	opts (t/trace (merge *default-opts* (apply array-map options)))]
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
    (if (:body opts) ; better check
      (mspit (.getOutputStream conn) (:body opts)))
    {:status (.getResponseCode conn)
     :body (mslurp (.getInputStream conn))}))

(defn http-call-service
  "Call a http service using http-req.
   Uses json as exchange format."
  ([service]
     (try
       (read-json
	(:body
	 (http-req service)))
       (catch Exception e
	 (raise :type :service-error))))
  ([service body]
     (try
       (read-json
	(:body
	 (http-req service {:body (json-str body)
			    :content-type "application/json"})))
       (catch Exception e
	 (raise :type :service-error)))))