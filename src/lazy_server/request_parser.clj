(ns lazy-server.request-parser
  (:require [clojure.string :refer [split join lower-case]]
            [lazy-server.parameter-decoder :refer [decode]]))

(defn parse-params [params outer-delimiter inner-delimiter]
  (reduce
    (fn [acc param]
      (let [[k v] (split param inner-delimiter)]
        (assoc acc (keyword (lower-case k)) v)))
    {}
    (split params outer-delimiter)))

(defn parse-status-line [raw-status-line]
  (let [[method url version] (split raw-status-line #" ")]
    {:method method
     :path (first (split url #"\?"))
     :query-params (parse-params (decode (second (split url #"\?"))) #"&" #"=")
     :http-version version}))

(defn parse [status-and-headers]
  (assoc
    (parse-status-line (first status-and-headers))
    :headers (parse-params (join \newline (rest status-and-headers)) #"\n" #": ")))
