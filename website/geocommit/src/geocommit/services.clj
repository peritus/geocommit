(ns
    geocommit.services
  (:import (java.net URI))
  (:use geocommit.config
	geocommit.core
	geocommit.http
	clojure.contrib.condition)
  (:require [clojure.contrib.str-utils2 :as s]
	    [clojure.contrib.trace :as t]))

(defn- split
  [s n]
  (list (s/take s n) (s/drop s n)))

(defn- github-api
  [repository-url query & parts]
  (if-let [^String repo-param
	   (last (re-find #"([\w\d\-\_]+/[\w\d\-\_]+)\.git$"
			  repository-url))]
    (str (.normalize
	  (URI. (apply str
		       (interpose "/"
				  (concat (list (get-config :api :github)
						query repo-param)
					  parts))))))))

(defprotocol Service
  "A standard service"
  (identifier
   [this]
   "Return a couchdb document identifier from the payload")
  (repository-url
   [this]
   "Return the repository url of the payload")
  (unify
   [this]
   "Maps the payload to a unified map. The returned map has a set of keys
    that every service must support.

    The map structure is
     {:identifier
      :name
      :description
      :url
      :repository-url
      :commits [{:message :revision :author}]}")
  (vcs [this])
  (check-commit [this c]))

(deftype Github [payload]
  Service
  
  (identifier
   [^Github this]
   (if-let [^String url (-> payload :repository :url)]
     (.replaceFirst url "(http://|https://)" "")))
  
  (repository-url
   [^Github this]
   (if-let [^String url (-> payload :repository :url)]
     (str (.replaceFirst url "(http://|https://)" "git://") ".git")))

  (unify
   [^Github this]
   {:identifier     (identifier this)
    :description    (-> payload :repository :description)
    :name           (-> payload :repository :name)
    :url            (-> payload :repository :url)
    :repository-url (repository-url this)
    :vcs            "git"
    :commits (map
	      (fn [{id :id {name :name mail :email} :author message :message}]
		{:revision id
		 :author   (str name " <" mail ">")
		 :message  message})
	      (:commits payload))})

  (vcs
   [^Github this]
   :git)
  
  (check-commit
   [^Github this c]
   (raise :type :not-yet-implemented)))

(deftype Bitbucket [payload]
  Service
  (identifier   
   [^Bitbucket this]
   (if (contains-all? payload [:repository :absolute_url])
     (str "bitbucket.org"
	  (-> payload :repository :absolute_url))))
	     
  (repository-url
   [^Bitbucket this]
   (if (contains-all? payload [:repository :absolute_url])
     (str "http://bitbucket.org"
	  (-> payload :repository :absolute_url))))

  (unify
   [^Bitbucket this]
   {:identifier     (identifier this)
    :description    nil
    :name           (-> payload :repository :name)
    :url            (-> payload :repository :website)
    :repository-url (repository-url this)
    :vcs            "mercurial"
    :commits (map
	      (fn [{raw_node :raw_node author :author message :message}]
		{:revision raw_node
		 :author   author
		 :message  message})
	      (:commits payload))})

  (vcs
   [^Bitbucket this]
   :mercurial)

  (check-commit
   [^Bitbucket this c]
   (raise :type :not-yet-implemented)))