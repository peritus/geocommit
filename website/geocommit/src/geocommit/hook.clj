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
	clojure.contrib.condition
	[ring.util.servlet :only [defservice]])
  (:require [compojure.route :as route]
	    [appengine-magic.core :as ae]
	    [clojure.contrib.trace :as t]
	    [clojure.contrib.http.agent :as agent])
  (:import java.net.URI))

(def *couchdb* (get-config :databases :geocommits))
(defstruct repository :_id :identifier :name :description :repository-url :vcs :scanned :type)

(defn- contains-all? [val & keys]
  (every? #(= true %)
	  (map #(clojure.core/contains? val %) keys)))

(defn is-tracked? [ident]
  (map? (couch-get *couchdb* (str "repository:" ident))))

(defn- scan [ident name desc repourl vcs]
  (couch-add *couchdb*
	     (struct repository
		     (str "repository:" ident)
		     ident name desc repourl vcs false "repository")))
  
(defn- guess-origin [payload]
  (cond
   (contains-all? payload :broker :service) :bitbucket
   (contains-all? payload :before :ref) :github))

(defn- bitbucket-update-parser [ident commits]
  (let [ctx (remove nil? (map #(parse-geocommit ident (%1 :node) (%1 :author) (%1 :message) (%1 :message)) commits))]
    (if (empty? ctx)
      nil ctx)))

(defn- bitbucket [ident payload]
  (if (is-tracked? ident)
    (if-let [ctx (bitbucket-update-parser ident (payload :commits))]
      (if-let [res (couch-bulk-update *couchdb* ctx)]
	{:status 201}
	{:status 400})
      {:status 200})
    (scan ident
	  (-> payload :repository :name)
	  (-> payload :repository :slug)
	  (-> payload :repository :absolute_url)
	  "mercurial")))

(defn string-json [a]
  (let [str (agent/string a)]
    (try
      (read-json str)
      (catch Exception e
	(raise :type :parse-error)))))

(defn- ident-to-repository [ident]
  (str "git://" ident " .git"))

(defn- ident-from-url [url]
  (if url
    (.replaceFirst url "(http://|https://)" "")
    (raise :type :parse-error)))

(defn- github-api-url [ident sub & rest]
  (str (.normalize (URI. (str
			  (get-config :api :github)
			  sub
			  (.replaceFirst ident "github\\.com/" "")
			  "/"
			  (apply str rest))))))
  
(defn- github-notes-commit [repository-url]
  (:refs/notes/geocommit (string-json (agent/http-agent (get-config :api :fetchservice)
							:method "POST"
							:body (json-str {:repository-url repository-url})))))

(defn- github-fetch-note [ident note-commit id]
  (-> (string-json (agent/http-agent (str (github-api-url ident "/blob/show/" note-commit "/" id))))
      :blob :data))

(defn- parse-github-geocommit [ident notehash commit]
  (let [{id :id {name :name mail :email} :author message :message}
	commit]
    (parse-geocommit ident id (str name " <" mail ">") message
		     (github-fetch-note ident notehash id))))

(defn github-update-parser [ident commits]
  (if-let [notehash (github-notes-commit (ident-to-repository ident))]
    (if-let [ctx (remove nil? (map #(parse-github-geocommit ident notehash %) commits))]
      (if (empty? ctx)
	nil ctx))))
  
(defn- github [ident payload]
  (if (is-tracked? ident)
    (if-let [ctx (t/trace (github-update-parser ident (:commits payload)))]
      (if-let [res (couch-bulk-update *couchdb* ctx)]
	{:status 201}
	{:status 400})
      {:status 200})
    (scan ident
	  (-> payload :repository :name)
	  (-> payload :repository :description)
	  (str "git://" ident ".git")
	  "git")))

(defn app-hook [rawpayload]
  (let [payload (read-json rawpayload)]
    (handler-case :type
	(condp = (guess-origin payload)
	    :github (github (ident-from-url (-> payload :repository :url)) payload)
	    :bitbucket (bitbucket (str "bitbucket.org/"
				       (-> payload :repository :absolute_url))
				  payload))
      (handle :parse-error
	{:status 400}))))
