					; geocommit bitbucket POST hook
					; (c) 2010 the geocommit project
					; (c) 2010 David Soria Parra
					; Licensed under the terms of the MIT License
(ns geocommit.core)
(defn- number [n]
  (Float/valueOf n))

(def *geocommit-regex*
     #"geocommit(\(\d+\.\d+\)): (.+,\s)(long|lat|acc|src) ([\d\w\.]+);")
(def *geocommit-subex*
     #"(long|lat|hacc|src) ([\d\w\.]+),")

(defstruct commit :repository :commit :message :author :latitude :longitude :horizontal-accurancy :source)

(defn parse-geocommit [repo hash author message]
  (when-let [[_ version options k v]
	     (re-find *geocommit-regex* message)]
    (let [{lat "lat" long "long" hacc "hacc" src "src"}
	  (merge
	   (apply merge (map #(hash-map (get %1 1) (get %1 2))
			     (re-seq *geocommit-subex* options)))
	   {k v})]
      (struct commit repo hash message author (number lat) (number long) (number hacc) src))))
