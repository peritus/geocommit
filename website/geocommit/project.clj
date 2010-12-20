(defproject geocommit "1.0.0-SNAPSHOT"
  :description "the geocommit project"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [compojure "0.5.3"]
		 [commons-validator/commons-validator "1.3.1"]
		 [oro/oro "2.0.8"]
		 [ring/ring "0.3.5"]]
  :dev-dependencies [[autodoc "0.7.1"]
                     [swank-clojure "1.2.0"]
		     [appengine-magic "0.3.1"]]
  :repositories ["java" "http://download.java.net/maven/2/"]
  :autodoc { :name "geocommit", :page-title "geocommit post service hook",
  	     :copyright "2010 David Soria Parra"})

