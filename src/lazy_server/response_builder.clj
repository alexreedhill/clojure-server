(ns lazy-server.response-builder
  (:require [clojure.string :refer [join]]))

(def status-messages
  { 200 "OK"
   404 "Not Found"})

(defn build [raw-response]
  (join "\r\n\n"
        [(str "HTTP/1.1 "
              (raw-response :code)
              " " (status-messages (raw-response :code)))
        (raw-response :body)]))

