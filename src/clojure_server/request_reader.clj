(ns clojure-server.request-reader
  (:require [clojure-server.request-parser :refer [parse]]
            [clojure.java.io :refer [reader writer]]
            [clojure.string :refer [blank?]]))

(defn read-until-body [in]
  (loop [request []]
    (if (and (blank? (last request)) (> (count request) 1))
      request
      (recur (conj request (.readLine in))))))

(defn read-body [in content-length]
  (let [body (char-array content-length)]
    (.read in body 0 content-length)
    (apply str body)))

(defn get-content-length [headers]
  (try
    (Integer/parseInt (headers :content-length))
    (catch NumberFormatException e
      0)))

(defn read-request [client-socket]
  (let [in (reader client-socket)
        status-and-headers (parse (read-until-body in))
        content-length (get-content-length (status-and-headers :headers))]
    (if (> content-length 0)
      (assoc status-and-headers
             :body (read-body in content-length))
      status-and-headers)))

