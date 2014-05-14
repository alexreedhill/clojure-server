(ns clojure-server.server
  (:require [clojure.java.io :refer [reader writer]])
  (:import (java.net ServerSocket InetAddress))
  (:gen-class :main true))

(defn open-server-socket [port address]
  (ServerSocket. port 0 (InetAddress/getByName address)))

(defn listen [server-socket]
  (try
    (.accept server-socket)
    (catch Exception e (println (str "Exception: " e)))))

(defn read-request [client-socket]
  (let [request (line-seq (reader client-socket))]
    request))

(defn run [server-socket]
  (read-request (listen server-socket)))

(defn write-response [request server-socket]
  (with-open [writer (writer server-socket)]
    (.write writer request)))

(def keep-going (atom true))

(defn -main [& args]
  (println "Server starting")
  (with-open [server-socket (open-server-socket 5000 "localhost")]
    (while @keep-going
      (write-response (run server-socket) server-socket)
    (println "Server stopping"))))
