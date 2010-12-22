					; (c) 2010 David Soria Parra
					; Licensed under the terms of the MIT License

(ns
  #^{:doc "A simple CouchDB Wrapper",
     :author "David Soria Parra"}
  experimentalworks.couchdb
  (:use	clojure.contrib.json,
	clojure.contrib.base64,
	clojure.contrib.logging)
  (:require [clojure.contrib.trace :as t])
  (:import (java.io InputStreamReader BufferedReader PrintWriter BufferedWriter OutputStreamWriter)
	   (java.net URL URLEncoder)))

; evil workaround for appengine. we cannot use contrib.duck-streams as
; they use Socket which is forbidden
(defn- mslurp [f]
  (with-open [#^BufferedReader r (BufferedReader. (InputStreamReader. f "UTF-8"))]
    (let [sb (StringBuilder.)]
      (loop [c (.read r)]
	(if (neg? c)
	  (str sb)
	  (do (.append sb (char c))
	      (recur (.read r))))))))		       

(defn- mspit [f content]
  (with-open [#^PrintWriter w (PrintWriter. (BufferedWriter. (OutputStreamWriter. f "UTF-8")))]
    (.print w content)))

(defn- build-result
  [conn]
  (try
    (condp >= (.getResponseCode conn)
      199 nil
      202 (read-json (mslurp (.getInputStream conn)))
      nil)))

(defmacro with-conn
  "Assumes that the first binding is a connection and closes
  the connection after executing the body."
  [bindings & body]
  `(let ~bindings
     (let [res# (do ~@body)]
	(.disconnect ~(first bindings))
	res#)))

(defn- couch-conn
  ([url method user pass]
     (let [conn (. (new URL url) openConnection)
	   mapping {:post "POST" :get "GET" :put "PUT" :delete "DELETE"}]
       (if (not (nil? user))
	 (.setRequestProperty conn "Authorization"
			      (str "Basic " (encode-str (str user ":" pass)))))
       (doto conn
	 (.setRequestProperty "Content-Type" "application/json")
	 (.setRequestProperty "Accept" "*/*")
	 (.setRequestProperty "User-Agent" "Clojure CouchDB Client 1.0.0")
	 (.setRequestProperty "Connection" "Close")
	 (.setRequestMethod (mapping method))
	 (.setInstanceFollowRedirects false)
	 (.setDoOutput true)
	 (.setDoInput true)
	 (.connect))
       conn)))

(defn couch-connection
  "Creates a new connection to a CouchDB server. The used method can
   be either :post, :get, :delete, or :put."
  [url method]
  (if-let [[_ prot user pass url] (re-find #"(https?://)([\w\d]+):([\w\d]+)@(.*)" url)]
    (couch-conn (str prot url) method user pass)
    (couch-conn url method nil nil)))

(defn couch-get
  "Get a document with the given id from the DB."
  [url id]
  (with-conn [conn (couch-connection (str url "/" (URLEncoder/encode id)) :get)]
    (build-result conn)))

(defn couch-update
  "Update the given document id with the new struct."
  [url id struct]
  (let [data (json-str struct)]
    (with-conn [conn (couch-connection (str url "/" id) :put)]
      (mspit (.getOutputStream conn) data)
      (build-result conn))))

(defn couch-add
  "Add a given struct to the CouchDB at the given url."
  [url struct]
  (let [data (json-str struct)]
    (with-conn [conn (couch-connection url :post)]
      (mspit (.getOutputStream conn) data)
      (build-result conn))))

(defn couch-bulk-update
  "Update multiple documents"
  [url xs]
  (when-let [res (couch-add (str url "/_bulk_docs") {:docs xs})]
    res))

(defn couch-delete
  "Delete a document of given id from the CouchDB instance at url."
  [url id]
  (with-conn [conn (couch-connection url :delete)]
    (build-result conn)))

(defn to-param [col]
  (apply str
	 (interpose "&"
		    (map #(str (name (key %1)) "=" (URLEncoder/encode (json-str (val %1)))) col))))

(defn- keyify [res]
  (apply merge
	 (map #(hash-map (%1 :id) %1) res)))

(defn couch-view
  "Returns a view with proper key value mappings"
  ([url design view]
     (couch-get (str url "/_design/" design "/_view/" view) ""))
  ([url design view keys options]
     (let [data (json-str {:keys keys})]
       (with-conn [conn (couch-connection (str url "/_design/" design "/_view/" view "?" (to-param options)) :post)]
	 (mspit (.getOutputStream conn) data)
	 (when-let [res (build-result conn)]
	   res)))))

					; (defn can-rollback [bla]
