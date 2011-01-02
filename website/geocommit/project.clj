(defproject geocommit "1.0.0-SNAPSHOT"
  :description "geocommit.com HTTP API"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [compojure "0.5.3"]
		 [clj-http "0.1.2-ae"]
		 [commons-validator/commons-validator "1.3.1"]
		 [oro/oro "2.0.8"]
		 [ring/ring "0.3.5"]]
  :dev-dependencies [[autodoc "0.7.1"]
                     [swank-clojure "1.2.0"]
		     [appengine-magic "0.3.1"]]
  :aot [geocommit.app_servlet geocommit.services]
  :repositories ["java" "http://download.java.net/maven/2/"]
  :autodoc {:name "geocommit",
	    :page-title "geocommit.com HTTP API",
	    :copyright "Copyright 2010 The Geocommit Project, David Soria Parra"
	    :description "The geocommit.com HTTP API is a simple rest API
to store/retrieve geocommits in/from the geocommit.com database"})