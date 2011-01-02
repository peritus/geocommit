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
	geocommit.services
	experimentalworks.couchdb
	clojure.contrib.logging
	clojure.contrib.json
	clojure.contrib.condition)
  (:require [clojure.contrib.trace :as t])
  (:import geocommit.services.Github
	   geocommit.services.Bitbucket))

(def *couchdb* (get-config :databases :geocommits))
(defrecord Repository [_id identifier name description repository-url vcs scanned type])

(defn is-tracked?
  "Check if the given repository identifier is already tracked"
  [ident]
  (map? (couch-get *couchdb* (str "repository:" ident))))

(defn unified->repository
  [unified]
  (if-let [{:keys [identifier name description repository-url vcs]} unified]
    (Repository. (str "repository:" identifier)
		 identifier
		 name
		 description
		 repository-url
		 vcs
		 false
		 "repository")))

(defn guess-origin
  "Heuristic to determine the origin of the hook request.
   Returns :github or :bitbucket"
  [payload]
  (cond
   (contains-all? payload [:repository :slug]) ::bitbucket
   (contains-all? payload :before :ref) ::github))

(defmulti service guess-origin)
(defmethod service ::github
  [payload]
  (Github. payload))

(defmethod service ::bitbucket
  [payload]
  (Bitbucket. payload))

(defmethod service :default
  [paylod]
  nil)

(defmulti update
  (fn [obj]
    (if (satisfies? Service obj)
      (vcs obj))))

(defmethod update :git
  [^Service service]
  (let [unified (unify service)]
    (and (:job (http-call-service (get-config :api :fetchservice)
				  {:identifier (:identifier unified)
				   :repository (:repository-url unified)
				   :commits (map #(:revision %) (:commits unified))}))
	 {:status 200})))

(defmethod update :mercurial
  [^Service service]
  (let [unified (unify service)]
    (if-let [ctx (not-empty
		  (remove nil?
			  (map
			   (fn [{:keys [revision author message]}]
			     (parse-geocommit (:identifier unified)
					      revision author
					      message message))
			   (:commits unified))))]
    (and (couch-bulk-update *couchdb* ctx)
	 {:status 201}))))
	   
(defmethod update :default
  [service]
  nil)

(defn scan
  [^Service service]
  (handler-case :type
    (let [c (unify service)
	  {:keys [identifier repository-url]} c]
      (and (couch-add *couchdb*
		      (unified->repository c))
	   (:job (http-call-service (get-config :api :initscan)
				    {:identifier identifier
				     :repository-url repository-url}))
	   {:status 200}))
    (handle :service-error
      #_(comment we intentionally ignore the service error and check with a
		 cronjob for unscanned jobs)
      true)))

(defn app-hook
  "API entry point for the github/bitbucket geocommit receive service."
  [rawpayload]
  (wrap error-handling
	(if (and rawpayload
		 (let [payload (read-json rawpayload)]
		   (let [s (service payload)]
		     (if (is-tracked? (identifier s))
		       (update s)
		       (scan s)))))
	  {:status 400})))