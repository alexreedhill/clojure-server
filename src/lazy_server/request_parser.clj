(ns lazy-server.request-parser
  (:require [lazy-server.parameter-decoder :refer [decode]]
            [clojure.string :refer [split join]]))

(defn parse-params [params outer-delimiter inner-delimiter]
  (reduce
    (fn [acc param]
      (let [[k v] (split param inner-delimiter)]
        (assoc acc k (decode v))))
    {}
    (split params outer-delimiter)))

(defn parse-headers [headers]
  (if-let [header-string (join "\n" headers)]
    (parse-params header-string #"\n" #": ")))

(defn parse-query [query]
  (if query
    (parse-params query #"&" #"=")))

(defn parse-status-line [raw-status-line]
  (let [[method url version] (split raw-status-line #" ")
        [path query] (split url #"\?")]
    {:method method
     :path  path
     :query-params (parse-query query)
     :http-version version}))

(defn parse [status-and-headers]
  (assoc
    (parse-status-line (first status-and-headers))
    :headers (parse-headers (rest status-and-headers))))
