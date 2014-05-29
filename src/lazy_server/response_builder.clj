(ns lazy-server.response-builder
  (:require [clojure.string :refer [join]]))

(def status-messages
  { 200 "OK"
    404 "Not Found"})

(defn build [raw-response]
  (str (join " "
    [(raw-response :code)
    (status-messages (raw-response :code))
    "HTTP/1.1\r\n\n"])
    (raw-response :body)))

