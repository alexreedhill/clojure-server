(ns lazy-server.server
  (:require [lazy-server.request-reader :refer [read-request]]
            [lazy-server.file-interactor :refer [log-request]]
            [lazy-server.spec-helper :refer [bytes-to-string]])
  (:import (java.net Socket ServerSocket InetAddress)
           (java.io OutputStream BufferedOutputStream DataOutputStream)
           (java.util.concurrent Executors))
  (:gen-class :main true))

(def public-dir "public/")

(defn open-server-socket [port address]
  (ServerSocket. (read-string port) 0 (InetAddress/getByName address)))

(defn write-response [request router client-socket]
  (with-open [out (java.io.DataOutputStream.
                    (java.io.BufferedOutputStream.
                      (.getOutputStream client-socket)))]
    (let [response (.getBytes "HTTP/1.1 200 OK\r\n\n")]
      (try
        (catch IllegalArgumentException e))
      (.write out response 0 (count response)))))

(def keep-going (atom true))

(defn handle-request [request pool client-socket args]
  (println "pool: " pool)
  (.execute pool
            #(write-response request (nth args 2) client-socket)))

(defn -main [& args]
  (def pool (Executors/newFixedThreadPool 8))
  (def public-dir (nth args 3))
  (let [server-socket (open-server-socket (first args) (second args))]
    (println "Lazy server listening...")
    (while (not (.isClosed server-socket))
       (try
         (let [client-socket (.accept server-socket)
               request (read-request client-socket)]
           (handle-request request pool client-socket args))
         (catch Exception e (println "Exception: " e))))))
