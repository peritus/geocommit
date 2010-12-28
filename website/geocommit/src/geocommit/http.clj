(ns #^{:doc "geocommit HTTP connections. Appengine compatible.",
       :author "David Soria Parra"}
  geocommit.http
  (:use clojure.contrib.json
	clojure.contrib.condition)
  (:require [clojure.contrib.trace :as t]
	    [clj-http.client :as client]))

(defn http-call-service
  "Call a http service using http-req.
   Uses json as exchange format."
  ([service]
     (try
       (read-json
	(:body
	 (client/get service)))
       (catch Exception e
	 (raise :type :service-error))))
  ([service body]
     (try
       (read-json
	(:body
	 (client/post service {:body (json-str body)
			       :content-type :json})))
       (catch Exception e
	 (raise :type :service-error)))))