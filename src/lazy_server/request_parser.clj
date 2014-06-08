(ns lazy-server.request-parser
  (:require [clojure.string :refer [split join lower-case]]
            [lazy-server.parameter-decoder :refer [decode]]))

(defn parse-params [params outer-delimiter inner-delimiter]
  (reduce
    (fn [acc param]
      (let [[k v] (split param inner-delimiter)]
        (assoc acc k (decode v))))
    {}
    (split params outer-delimiter)))

(defn parse-headers [headers]
  (let [header-string (join "\n" headers)]
    (if header-string
      (parse-params header-string #"\n" #": "))))

(defn parse-query-string [url]
  (let [query-string (second (split url #"\?"))]
    (if query-string
      (parse-params query-string #"&" #"="))))

(defn parse-status-line [raw-status-line]
  (let [[method url version] (split raw-status-line #" ")]
    {:method method
     :path (first (split url #"\?"))
     :query-params (parse-query-string url)
     :http-version version}))

(defn parse [status-and-headers]
  (assoc
    (parse-status-line (first status-and-headers))
    :headers (parse-headers (rest status-and-headers))))
