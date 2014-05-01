(ns clojure-server.request)
(use '[clojure.string :only (split)])

(defn parse-status-line [raw-status-line]
  (let [split-status-line (split raw-status-line #"\s+")]
    {:method (split-status-line 0)
     :path (split-status-line 1)
     :http-version (split-status-line 2)}))

(defn parse-headers [raw-headers]
  {:headers (split raw-headers #"\n+")})

(defn parse [raw-request]
  (let [split-request (split raw-request #"\r\n+")]
      (merge
        (parse-status-line (nth split-request 0))
        (parse-headers (nth split-request 1 ""))
        {:body (nth split-request 2 "")})))
