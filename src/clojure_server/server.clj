(ns clojure-server.server
  (:require [clojure.java.io :refer [reader]])
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
    (println "REQUEST: " (apply str request))
    request))

(defn run [port address]
  (read-request (listen (open-server-socket port address))))

(defn -main [& args]
  (println "Server starting")
  (while true
    (run 5000 "localhost"))
  (println "Server stopping"))
