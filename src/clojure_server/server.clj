(ns clojure-server.server
  (:require [clojure.java.io :refer [reader writer]]
            [clojure.string :refer [blank?]])
  (:import (java.net Socket ServerSocket InetAddress ConnectException))
  (:gen-class :main true))

(defn open-server-socket [port address]
  (ServerSocket. port 0 (InetAddress/getByName address)))

(defn listen [server-socket]
  (try
    (.accept server-socket)
    (catch Exception e (println (str "Exception: " e)))))

(defn read-request [client-socket]
  (let [in (reader client-socket)]
    (loop [request []]
      (if (and (blank? (last request)) (> (count request) 1))
        request
        (recur (conj request (.readLine in)))))))

(defn write-response [request client-socket]
  (with-open [w (writer client-socket)]
    (.write w (apply str request))))

(def keep-going (atom true))

(defn -main [& args]
  (with-open [server-socket (open-server-socket (first args) (second args))]
    (while @keep-going
      (let [client-socket (listen server-socket)]
        (write-response (read-request client-socket) client-socket)))))
