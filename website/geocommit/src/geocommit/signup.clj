					; geocommit signup
					; (c) 2010 The Geocommit Project
					; (c) 2010 David Soria Parra
					; Licensed under the terms of the MIT License
(ns geocommit.signup
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:use geocommit.core
	experimentalworks.couchdb
	clojure.contrib.logging
	clojure.contrib.json
	[ring.util.servlet :only [defservice]])
  (:import (org.apache.commons.validator EmailValidator)
	   (java.util UUID))
  (:require [compojure.route :as route]
	    [clojure.contrib.trace :as t]
	    [appengine-magic.services.mail :as mail]))

(def *couchdb*
     "http://geocommit:geocommit@dsp.couchone.com/invites")

(defstruct invite :_id :date :mail :invitecode :active :verifycode :verified)

(defn- validate-email [mail]
  (.isValid (EmailValidator/getInstance) mail))

(defn- create-verify-code []
  (.toString (UUID/randomUUID)))

(defn- verify-code [code]
  (if-let [res (couch-view *couchdb* "views" "verifycode" [code] {:include_docs true})]
    (if (= (count (res :rows)) 1)
      (first (res :rows)))
    nil))

(defn app-verify-hook [code]
  (if-let [res (verify-code code)]
    (do
      (couch-update *couchdb* (res :id) (assoc (res :doc) :verified true))
      {:status 200 :body "Thank you. Verification successful"})
    {:status 200 :body "Cannot verify."}))

(defn app-signup [mailaddr]
  (let [code (create-verify-code)]
    (if (validate-email mailaddr)
      (can-rollback [res (couch-add *couchdb*
				    (struct invite
					    (str "mail:" mailaddr)
					    (isodate) mailaddr nil false code false))]
		    (mail/send (mail/make-message
				:from "experimentalworks@googlemail.com"
				:to mailaddr
				:subject "Welcome to geocommit.com. Please verify your invitation request."
				:text-body (str "Follow the link to verify your invitation request\n\n"
						"http://geocommit.com/signup/verify/" code))))
	{:status 201})
      {:status 400}))