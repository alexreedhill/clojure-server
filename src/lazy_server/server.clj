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

(defn write-response [request router ^Socket client-socket]
  (with-open [out (java.io.DataOutputStream.
                    (java.io.BufferedOutputStream.
                      (.getOutputStream client-socket)))]
    (let [response (router request)]
      (try
        (catch IllegalArgumentException e))
      (.write ^DataOutputStream out response 0 (count response)))))

(defn handle-request [pool client-socket args]
  (.execute ^java.util.concurrent.ExecutorService pool
            #(let [request (read-request client-socket)]
               (log-request request (str public-dir "log.txt"))
               (write-response request (nth args 2) client-socket))))

(defn -main [& args]
  (let [server-socket (open-server-socket (first args) (second args))
        pool (Executors/newFixedThreadPool 8)]
    (println "Lazy server listening...")
    (while (not (.isClosed ^ServerSocket server-socket))
       (try
         (let [client-socket (.accept ^ServerSocket server-socket)]
           (handle-request pool client-socket args))
         (catch Exception e (println "Exception: " e))))))
