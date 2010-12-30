					; geocommit.com HTTP signup API
					; (c) 2010 David Soria Parra <dsp+geocommit@experimentalworks.net>
					;          Nils Adermann <naderman+geocommit@naderman.de>
					;          Filip Noetzel <filip+geocommit@j03.de>
					; Licensed under the terms of the MIT License
(ns #^{:doc "HTTP signup API functions",
       :author "David Soria Parra"}
  geocommit.signup
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:use geocommit.core
	geocommit.config
	compojure.core
	experimentalworks.couchdb
	clojure.contrib.logging
	clojure.contrib.json
	clojure.contrib.java-utils
	[ring.util.servlet :only [defservice]])
  (:import (org.apache.commons.validator EmailValidator)
	   (java.util UUID))
  (:require [compojure.route :as route]
	    [clojure.contrib.trace :as t]
	    [appengine-magic.services.mail :as mail]))

(def *couchdb* (get-config :databases :invites))

(defrecord Invite [_id date mail invitecode active verifycode verified type])

(defn- validate-email
  "Check if the given email address is valid"
  [mail]
  (.isValid (EmailValidator/getInstance) mail))

(defn- create-verify-code []
  (.toString (UUID/randomUUID)))

(defn- verify-code [code]
  (if-let [res (couch-view *couchdb* "views" "verifycode" [code] {:include_docs true})]
    (if (= (count (res :rows)) 1)
      (first (res :rows)))
    nil))

(defn app-verify-hook
  "API entry point to verify a signup code."
  [code]
  (if-let [res (verify-code code)]
    (do
      (couch-update *couchdb* (res :id) (assoc (res :doc) :verified true))
      {:status 200 :body "Thank you. Verification successful"})
    {:status 200 :body "Cannot verify."}))

(defn app-signup
  "API entry point to signup a mail address."
  [mailaddr]
  (let [code (create-verify-code)]
    (if (and
	 (validate-email mailaddr)
	 (couch-add *couchdb*
		    (Invite.
		     (str "mail:" mailaddr)
		     (isodate) mailaddr nil false code false "invite")))
      (do
	(mail/send (mail/make-message
		    :from "geocommit-team@j03.de"
		    :to mailaddr
		    :subject "Welcome to geocommit.com. Please verify your invitation request."
		    :text-body (str "Welcome to geocommit.com.\n\nFollow the link to verify your invitation request\n\n"
				    "http://geocommit.com/signup/verify/" code)))
	{:status 201})
      {:status 400})))
