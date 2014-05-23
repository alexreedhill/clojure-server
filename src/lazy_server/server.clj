(ns lazy-server.server
  (:require [lazy-server.request-reader :refer [read-request]]
            [clojure.java.io :refer [writer]])
  (:import (java.net Socket ServerSocket InetAddress ConnectException))
  (:gen-class :main true))

(defn open-server-socket [port address]
  (ServerSocket. port 0 (InetAddress/getByName address)))

(defn listen [server-socket]
  (try
    (.accept server-socket)
    (catch Exception e (println (str "Exception: " e)))))

(defn write-response [request client-socket]
  (with-open [w (writer client-socket)]
    (.write w (str
                (request :method) " "
                (request :path) " "
                (request :http-version) " "
                (request :headers) " "
                (request :body)))))

(def keep-going (atom true))

(defn -main [& args]
  (with-open [server-socket (open-server-socket (first args) (second args))]
    (while @keep-going
      (let [client-socket (listen server-socket)]
        (write-response (read-request client-socket) client-socket)))))
