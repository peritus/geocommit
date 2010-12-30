					; geocommit.com HTTP api
					; (c) 2010 David Soria Parra <dsp+geocommit@experimentalworks.net>
					;          Nils Adermann <naderman+geocommit@naderman.de>
					;          Filip Noetzel <filip+geocommit@j03.de>
					; Licensed under the terms of the MIT License
(ns #^{:doc "HTTP API",
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