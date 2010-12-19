					; geocommit signup
					; (c) 2010 The Geocommit Project
					; (c) 2010 David Soria Parra
					; Licensed under the terms of the MIT License
(ns geocommit.signup
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:use geocommit.core
	compojure.core
	experimentalworks.couchdb
	clojure.contrib.logging
	clojure.contrib.json
	[ring.util.servlet :only [defservice]])
  (:import org.apache.commons.validator.EmailValidator)
  (:require [compojure.route :as route]
	    [clojure.contrib.trace :as t]))

(def *couchdb*
     "http://geocommit:geocommit@dsp.couchone.com/invites")

(defstruct invite :_id :date :mail :invitecode :uses)

(defn validate-email [mail]
  (.isValid (EmailValidator/getInstance) mail))

(defroutes signup-routes
  (POST "*" [mailaddr] (if (and
			    (validate-email mailaddr)
			    (couch-add *couchdb* (struct invite (str "mail:" mailaddr) (isodate) mailaddr nil nil)))
			 {:status 201}
			 {:status 400}))
  (route/not-found "not a valid request"))

(defservice signup-routes)