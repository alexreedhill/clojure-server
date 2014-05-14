(ns clojure-server.server-spec
  (:require [clojure-server.server :refer :all])
  (:require [clojure.java.io :refer [reader writer]])
  (:require [speclj.core :refer :all])
  (:import (java.net Socket ServerSocket InetAddress ConnectException)))

(defn connect-socket [port address]
  (try
    (Socket. address port)
    (catch ConnectException e
      (connect-socket port address))))

(defn send-request [socket request]
  (do
    (println "sending request")
    (with-open [writer (writer socket)]
      (.write (writer socket) request))))

(describe "opens sockets"
  (it "opens a java.net.ServerSocket on the correct port and address"
    (with-open [server-socket (open-server-socket 5000 "localhost")]
      (should= ServerSocket (class server-socket))
      (should= (.getLocalPort server-socket) 5000)
      (should= (.getInetAddress server-socket) (InetAddress/getByName "localhost")))))

(describe "reads request"
  (it "creates line sequence from client input"
    (pvalues
      (-main)
      (do
        (with-open [socket (connect-socket 5000 "localhost")]
          (send-request socket "foo")
          (should= (line-seq (reader socket)) "foo")))))

  (it "reads multiline request"
    (pvalues
      (-main)
      (do
        (with-open [socket (connect-socket 5000 "localhost")]
          (send-request socket "GET / HTTP/1.1\r\nFoo: Bar")
          (println "keep going value: " @keep-going)
          (should= (line-seq (reader socket)) '("GET / HTTP/1.1" "Foo: Bar"))))))

  (it "reads multiple requests"
    (pvalues
      (-main)
      (do
        (with-open [socket (connect-socket 5000 "localhost")]
          (loop [acc []]
            (if (= (count acc) 2)
              (do
                (should= ['("GET / HTTP/1.1" "Foo: Bar") '("GET / HTTP/1.1" "Foo: Bar")] acc)
                (send-request socket "GET / HTTP/1.1\r\nFoo: Bar")
                (recur
                  (conj acc (line-seq (reader socket))))))))))))

