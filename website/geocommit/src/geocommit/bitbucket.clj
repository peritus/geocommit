					; geocommit bitbucket POST hook
					; (c) 2010 the geocommit project
					; (c) 2010 David Soria Parra
					; Licensed under the terms of the MIT License
(ns geocommit.bitbucket
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:use geocommit.core
	compojure.core
	experimentalworks.couchdb
	clojure.contrib.logging
	clojure.contrib.json
	[ring.util.servlet :only [defservice]])
  (:require [compojure.route :as route]))

(defn is-tracked? [repo] true)

(def *couchdb*
     "http://geocommit:geocommit@dsp.couchone.com/geocommit")

(defn parse-bitbucket-update [url commits]
  (let [ctx (remove nil? (map #(parse-geocommit url (%1 :node) (%1 :author) (%1 :message)) commits))]
    (if (empty? ctx)
      nil ctx)))

(defroutes main-routes
  (POST "*" [payload]
	(let [json (read-json payload)
	      commits (json :commits)
	      repo (json :repository)
	      url (str "http://bitbucket.org" (repo :absolute_url))]
	  (if (and (is-tracked? url) (vector? commits))
	    (if-let [ctx (parse-bitbucket-update url commits)]
	      (if-let [res (couch-bulk-update *couchdb* ctx)]
		{:status 201} {:status 400})
	      {:status 200})
	    {:status 200})))
  (route/not-found "not a valid request"))

(defservice main-routes)