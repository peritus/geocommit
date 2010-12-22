					; geocommit.com HTTP API main api
					; (c) 2010 The Geocommit Project
					; (c) 2010 David Soria Parra
					; Licensed under the terms of the MIT License
(ns #^{:doc "HTTP API"
     :author "David Soria Parra"}
  geocommit.api
  (:use geocommit.config
	compojure.core
	clojure.contrib.logging
	clojure.contrib.json
	experimentalworks.couchdb)
  (:require [clojure.contrib.trace :as t]))

(def *couchdb* (get-config :databases :geocommits))

(defn app-api-geocommits
  [payload]
  (json-str (:rows (couch-view *couchdb* "views" "geocommits"))))