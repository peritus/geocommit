(defproject geocommit "1.0.0-SNAPSHOT"
  :description "the geocommit project"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [compojure "0.5.3"]
		 [ring/ring "0.3.5"]]
  :dev-dependencies [[autodoc "0.7.1"]
                     [swank-clojure "1.2.0"]
                     [ring/ring-jetty-adapter "0.3.4"]
   		             [uk.org.alienscience/leiningen-war "0.0.11"]]
  :aot [geocommit.bitbucket geocommit.core experimentalworks.couchdb]
  :compile-path "war/WEB-INF/classes"
  :library-path "war/WEB-INF/lib"
  :autodoc { :name "geocommit", :page-title "geocommit post service hook",
  	     :copyright "2010 David Soria Parra"})
					;  :main geocommit.run
