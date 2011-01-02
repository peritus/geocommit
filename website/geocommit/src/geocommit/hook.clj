					; geocommit.com HTTP hook API
					; (c) 2010 David Soria Parra <dsp+geocommit@experimentalworks.net>
					;          Nils Adermann <naderman+geocommit@naderman.de>
					;          Filip Noetzel <filip+geocommit@j03.de>
					; Licensed under the terms of the MIT License
(ns #^{:doc "HTTP signup API functions",
       :author "David Soria Parra"}
  geocommit.hook
  (:use geocommit.core
	geocommit.config
	geocommit.http
	experimentalworks.couchdb
	clojure.contrib.logging
	clojure.contrib.json
	clojure.contrib.condition)
  (:require [appengine-magic.core :as ae]
	    [clojure.contrib.trace :as t]
	    [clojure.contrib.str-utils2 :as s])
  (:import (java.net URI URISyntaxException URL MalformedURLException)))

(def *couchdb* (get-config :databases :geocommits))
(defrecord Repository [_id identifier name description repository-url vcs scanned type])

(defn is-tracked?
  "Check if the given repository identifier is already tracked"
  [ident]
  (map? (couch-get *couchdb* (str "repository:" ident))))

(defn- sanitize-url
  [url]
  (try
    (str (.normalize (URI. (str (URL. url)))))
    (catch MalformedURLException mue
      (raise :type :uri-error))
    (catch URISyntaxException use
      (raise :type :uri-error))))

(defn- send-scan
  "Send a scan to the fetch service"
  [ident repository]
  (:job (http-call-service (get-config :api :initscan)
			   {:identifier ident
			    :repository-url repository})))

(defn- scan
  "Add a scan job to the database"
  [ident name desc repourl vcs]
  (and (couch-add *couchdb*
		  (Repository.
		   (str "repository:" ident)
		   ident name desc repourl vcs false "repository"))
       (handler-case :type
	 (send-scan ident repourl)
	 (handle :service-error
	   (comment we intentionally ignore the service error and check with a
		    cronjob for unscanned jobs)
	   true))))
  
(defn- guess-origin
  "Heuristic to determine the origin of the hook request.
   Returns :github or :bitbucket"
  [payload]
  (cond
   (contains-all? payload [:repository :slug]) :bitbucket
   (contains-all? payload :before :ref) :github))

;; Bitbucket handler
;;
(defn- bitbucket-update-parser
  "Parse bitbucket.org commits"
  [ident commits]
  (let [ctx (remove nil? (map #(parse-geocommit ident (%1 :raw_node) (%1 :author) (%1 :message) (%1 :message)) commits))]
    (if (empty? ctx)
      nil ctx)))
					;
;; Github Handler
;;
(defn- ident-from-url
  "Takes a URL like http://github.com/foo and returns a repository identifier.

   Example:
     http://github.com/dsp/geocommit -> github.com/dsp/gecommit"
  [url]
  (if url
    (.replaceFirst (sanitize-url url) "(http://|https://)" "")
    (raise :type :parse-error)))

(defn- github-notes-fetch
  "Return refs/notes/geocommit for a repository by using the fetchservice"
  [repository-url commits]
  (:job (http-call-service (get-config :api :fetchservice)
			   {:repository-url repository-url
			    :commits (map #(:id %) commits)})))

(defn- bitbucket
  [ident payload]
  (if (is-tracked? ident)
    (if-let [ctx (bitbucket-update-parser ident (:commits payload))]
      (if-let [res (couch-bulk-update *couchdb* ctx)]
	{:status 201}
	(raise :type :service-error))
      {:status 200})
    (if (scan ident
	      (-> payload :repository :name)
	      (-> payload :repository :description)
	      (sanitize-url (str "http://" ident))
	      "mercurial")
      {:status 200})))

(defn- github
  [ident payload]
  (let [url (str "git://"ident".git")
	commits (:commits payload)]
    (if (is-tracked? ident)
      (if-let [res (github-notes-fetch url
				       commits)]
	{:status 200}
	(raise :type :service-error))
      (if (scan ident
		(-> payload :repository :name)
		(-> payload :repository :description)
		url
		"git")
	{:status 200}))))

(defn app-hook
  "API entry point for the github/bitbucket geocommit receive service."
  [rawpayload]
  (if rawpayload
    (handler-case :type
      (try
	(if-let [payload (read-json (t/trace rawpayload))]
	  (condp = (guess-origin payload)
	      :github    (github (ident-from-url (-> payload :repository :url)) payload)
	      :bitbucket (bitbucket (str "bitbucket.org"
					 (-> payload :repository :absolute_url))
				    payload))
	  (raise :type :parse-error))
	(catch Exception e
	  (raise :type :service-error
		 :message (.getMessage e))))
	(handle :parse-error
	  (error (:message "parse error"))
	  {:status 400})
	(handle :uri-error
	  (with-logs
	    (println (:message *condition*))
	    (print-stack-trace *condition*)
	    {:status 400}))
	(handle :service-error
	  (with-logs
	    (println (:message *condition*))
	    (print-stack-trace *condition*)
	    {:status 500})))))