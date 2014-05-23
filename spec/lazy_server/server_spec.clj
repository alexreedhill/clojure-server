(ns lazy-server.server-spec
  (:require [lazy-server.server :refer :all]
            [clojure.java.io :refer [reader writer]]
            [speclj.core :refer :all])
  (:import (java.net Socket ConnectException)))

(defn connect-socket [port address]
  (try
    (Socket. address port)
    (catch ConnectException e
      (println "retrying socket connection")
      (connect-socket port address))))

(defn request-response [request]
  (with-open [socket (connect-socket 5000 "localhost")
              out (writer socket)
              in (reader socket)]
    (.write out request)
    (.flush out)
    (.readLine in)))

(describe "server"
  (before-all
    (future (-main 5000 "localhost")))

  (it "reads line from client input and responds"
    (should= "GET / HTTP/1.1 {: nil} "(request-response "GET / HTTP/1.1\r\n\n")))

  (it "reads a multiline request and responds"
    (should= "GET / HTTP/1.1 {:foo \"Bar\"} "
      (request-response "GET / HTTP/1.1\r\nFoo: Bar\r\n\n")))

  (it "reads multiple requests and responds"
    (loop [requests ()]
      (if (= (count requests) 2)
        (should= '("GET / HTTP/1.1 {: nil} " "GET / HTTP/1.1 {: nil} ") requests)
        (recur (conj requests (request-response "GET / HTTP/1.1\r\n\n"))))))

  (it "reads request with body"
    (should= "GET / HTTP/1.1 {:content-length \"3\"} Foo"
      (request-response "GET / HTTP/1.1\r\nContent-Length: 3\r\n\nFoo"))))

