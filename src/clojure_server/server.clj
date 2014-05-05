(ns clojure-server.server
  (:import (java.net ServerSocket InetAddress)))

(defn open-server-socket [port address]
  (ServerSocket. port 0 (InetAddress/getByName address)))

(defn listen [server-socket]
  (try
    (.accept server-socket)
    (catch Exception e (println (str "Exception: " e)))))
