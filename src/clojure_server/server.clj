(ns clojure-server.server
  (:require [clojure.java.io :refer :all])
  (:import (java.net ServerSocket InetAddress)))

(defn open-server-socket [port address]
  (ServerSocket. port 0 (InetAddress/getByName address)))

(defn listen [server-socket]
  (try
    (.accept server-socket)
    (catch Exception e (println (str "Exception: " e)))))

(defn read-request [client-socket]
  (line-seq (reader client-socket)))

(defn run [port address]
  (read-request (listen (open-server-socket port address))))
