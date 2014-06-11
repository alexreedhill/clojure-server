(ns lazy-server.server
  (:require [lazy-server.request-reader :refer [read-request]]
            [lazy-server.router :refer :all]
            [lazy-server.spec-helper :refer [bytes-to-string]]
            [clojure.java.io :refer [output-stream]])
  (:import (java.net Socket ServerSocket InetAddress)
           (java.io OutputStream BufferedOutputStream DataOutputStream))
  (:gen-class :main true))

(defn open-server-socket [port address]
  (ServerSocket. (read-string port) 0 (InetAddress/getByName address)))

(defn listen [server-socket]
  (try
    (.accept server-socket)
    (catch Exception e (println (str "Exception: " e)))))

(defn write-response [request router client-socket]
  (println "Incoming request: " request)
  (with-open [out (java.io.DataOutputStream.
                    (java.io.BufferedOutputStream.
                      (.getOutputStream client-socket)))]
    (let [response (router request)]
      (try
        (println "Outgoing response: " (bytes-to-string response))
        (catch IllegalArgumentException e))
      (.write out (bytes response) 0 (count response)))))

(def keep-going (atom true))

(defn -main [& args]
  (with-open [server-socket (open-server-socket (first args) (second args))]
    (println "Lazy server listening...")
    (while @keep-going
      (let [client-socket (listen server-socket)]
        (write-response (read-request client-socket) (nth args 2) client-socket))))
  (println "Lazy server stopping..."))
