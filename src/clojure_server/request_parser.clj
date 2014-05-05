(ns clojure-server.request-parser
  (:require [clojure.string :refer [split]])
  (:require [clojure-server.parameter-decoder :refer [decode]]))

(defn parse-params [params outer-delimiter inner-delimiter]
  (reduce
    (fn [acc param]
      (let [[k v] (split param inner-delimiter)]
        (assoc acc k v)))
    {}
    (split params outer-delimiter)))

(defn parse-status-line [raw-status-line]
  (let [split-status-line (split raw-status-line #" ")]
    {:method (split-status-line 0)
     :path (split-status-line 1)
     :query-params (parse-params (decode (second (split (split-status-line 1) #"\?"))) #"&" #"=")
     :http-version (split-status-line 2)}))

(defn parse [raw-request]
  (let [split-request (split raw-request #"\r\n")]
      (assoc
        (parse-status-line (nth split-request 0))
        :headers (parse-params (nth split-request 1 "") #"\n" #": ")
        :body (nth split-request 2 ""))))
