(ns clojure-server.request)
(use '[clojure.string :only (split)])

(defn parse [raw-request]
  (let [split-request (split raw-request #"\s+")]
    {:method (get split-request 0)
     :path (get split-request 1)
     :http-version (get split-request 2)}))

