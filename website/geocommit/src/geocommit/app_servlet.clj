					; geocommit.com HTTP API entry point
					; (c) 2010 The Geocommit Project
					; (c) 2010 David Soria Parra
					; Licensed under the terms of the MIT License
(ns #^{:doc "HTTP API entry point. This provides the main entry point for all routes to the
 services and dispatches the handler functions.",
     :author "David Soria Parra"}
  geocommit.app_servlet
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:use compojure.core
	[geocommit.hook :only [app-hook]]
	[geocommit.api :only [app-api-geocommits]]
	[geocommit.signup :only [app-verify-hook app-signup]])
  (:use [appengine-magic.servlet :only [make-servlet-service-method]]
	[appengine-magic.core :as ae])
  (:require [compojure.route :as route]))

(defroutes handler
  (GET "/api/geocommits" [payload] (app-api-geocommits payload))
  (POST "/api*" [payload] (app-hook payload))
  (GET "/signup/verify/:code" [code] (app-verify-hook code))
  (POST "/signup*" [mailaddr] (app-signup mailaddr))
  (route/not-found "not a valid request"))

(ae/def-appengine-app geocommit-application #'handler)
  
(defn -service [this request response]
  ((make-servlet-service-method geocommit-application) this request response))
