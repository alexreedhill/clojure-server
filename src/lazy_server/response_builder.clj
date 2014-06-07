(ns lazy-server.response-builder
  (:require [clojure.string :refer [join]]))

(def status-messages
  {200 "OK"
   404 "Not Found"})

(defn build [request response]
  (join "\r\n\n"
        [(str "HTTP/1.1 "
              (response :code) " "
              (status-messages (response :code)))
         (response :body)]))
