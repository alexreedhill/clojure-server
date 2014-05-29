(ns lazy-server.server-spec
  (:require [lazy-server.server :refer :all]
            [lazy-server.router :refer [defrouter]]
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

(defrouter test-router
  (GET "/" {:code 200}))

(describe "server"
  (before-all
    (future (-main "5000" "localhost" test-router)))

  (it "reads line from client input and responds"
    (should=  "200 OK HTTP/1.1" (request-response "GET / HTTP/1.1\r\n\n")))

  (it "reads a multiline request and responds"
    (should=  "200 OK HTTP/1.1"
      (request-response "GET / HTTP/1.1\r\nFoo: Bar\r\n\n")))

  (it "reads multiple requests and responds"
    (loop [requests ()]
      (if (= (count requests) 2)
        (should= '("200 OK HTTP/1.1" "200 OK HTTP/1.1") requests)
        (recur (conj requests (request-response "GET / HTTP/1.1\r\n\n"))))))

  (it "reads request with body"
    (should= "200 OK HTTP/1.1"
      (request-response "GET / HTTP/1.1\r\nContent-Length: 3\r\n\nFoo"))))

