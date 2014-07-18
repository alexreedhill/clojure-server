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

(defn write-response [request router public-dir client-socket]
  (let [out (java.io.DataOutputStream.
                    (java.io.BufferedOutputStream.
                      (.getOutputStream client-socket)))]
    (let [response (router request)]
      (.write out response 0 (count response))
      (.flush out)
      (.close out))))

(defn handle-request [client-socket args]
  (let [response (.getBytes "HTTP/1.1 200 OK\r\n\n")
        out (java.io.DataOutputStream.
              (java.io.BufferedOutputStream.
                (.getOutputStream client-socket)))]
    (.write out response 0 (count response))
    (.flush out)
    (.close out)))
  ;(let [request (read-request client-socket)]
    ;(write-response request (nth args 2) public-dir client-socket)
    ;(.close client-socket)))

(defn new-thread [pool fn]
  (println "pool: " pool)
  (.execute pool fn))

(defn new-thread-for-request [client-socket pool args]
  (new-thread pool #(handle-request client-socket args))

(defn -main [& args]
  (def public-dir (nth args 3))
  (def pool (Executors/newFixedThreadPool 8))
    (let [server-socket (open-server-socket (first args) (second args))]
      (println "Lazy server listening...")
      #(when-not (.isClosed server-socket)
         (try
           (let [client-socket (.accept server-socket)]
             (new-thread-for-request client-socket pool args))
           (catch Exception e (println "Exception: " e)))
         (recur)))))
