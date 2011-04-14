(defproject geocommit "1.0.0-SNAPSHOT"
  :description "geocommit.com HTTP API"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [compojure "0.5.3"]
                 [clj-http-ae "0.1.2"]
                 [commons-validator/commons-validator "1.3.1"]
                 [oro/oro "2.0.8"]
                 [ring/ring "0.3.5"]]
  :dev-dependencies [[swank-clojure "1.2.0"]
                     [appengine-magic "0.4.1"]]
  :aot [geocommit.app_servlet geocommit.services]
  :repositories {"java.net" "http://download.java.net/maven/2"})
