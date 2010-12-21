					; geocommit bitbucket POST hook
					; (c) 2010 the geocommit project
					; (c) 2010 David Soria Parra
					; Licensed under the terms of the MIT License
(ns geocommit.hook
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:use geocommit.core
	geocommit.config
	experimentalworks.couchdb
	clojure.contrib.logging
	clojure.contrib.json
	[ring.util.servlet :only [defservice]])
  (:require [compojure.route :as route]
	    [appengine-magic.core :as ae]
	    [clojure.contrib.trace :as t]))
(def *couchdb* (get-config :database :geocommits))

(defn- contains-all? [val & keys]
  (every? #(= true %)
	  (map #(clojure.core/contains? val %) keys)))

(defn is-tracked? [repo] true)

(defn- bitbucket-update-parser [url commits]
  (let [ctx (remove nil? (map #(parse-geocommit url (%1 :node) (%1 :author) (%1 :message)) commits))]
    (if (empty? ctx)
      nil ctx)))

(defn- guess-origin [payload]
  (cond
   (contains-all? payload :broker :service) :bitbucket
   (contains-all? payload :before :ref) :github))

(defn- bitbucket [url commits]
  (if (is-tracked? url)
    (if-let [ctx (bitbucket-update-parser url commits)]
      (if-let [res (couch-bulk-update *couchdb* ctx)]
	{:status 201}
	{:status 400})
      {:status 200})
    {:status 200}))

(defn- github [url commits]
  {:status 400})

(defn app-hook [payload]
  (let [json (read-json payload)]
    (condp = (guess-origin json)
	:github (github (json :url)
			(json :commits))
	:bitbucket (bitbucket (str "http://bitbucket.org/"
				   (json :repository))
			      (json :commits)))))