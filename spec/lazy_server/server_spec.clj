(ns lazy-server.server-spec
  (:require [lazy-server.server :refer :all]
            [lazy-server.router :refer [defrouter GET]]
            [clojure.java.io :refer [reader writer delete-file]]
            [speclj.core :refer :all])
  (:import (java.net Socket ConnectException)))

(defn connect-socket [port address]
    (Socket. address port))

(defn request-response [request]
  (with-open [socket (connect-socket 6000 "localhost")
              out (writer socket)
              in (reader socket)]
    (.write out request)
    (.flush out)
    (.readLine in)))

(defrouter test-router request
  (GET "/" {:code 200}))

(describe "server"
  (before-all
    (future (-main "6000" "localhost" test-router "public/")))

  (it "reads line from client input and responds"
    (should=  "HTTP/1.1 200 OK" (request-response "GET / HTTP/1.1\r\n\n")))

  (it "reads a multiline request and responds"
    (should=  "HTTP/1.1 200 OK"
      (request-response "GET / HTTP/1.1\r\nFoo: Bar\r\n\n")))

  (it "reads multiple requests and responds"
    (loop [requests ()]
      (if (= (count requests) 2)
        (should= '("HTTP/1.1 200 OK" "HTTP/1.1 200 OK") requests)
        (recur (conj requests (request-response "GET / HTTP/1.1\r\n\n"))))))

  (it "reads request with body"
    (should= "HTTP/1.1 200 OK"
      (request-response "GET / HTTP/1.1\r\nContent-Length: 3\r\n\nFoo")))

  (it "sets public directory"
    (should= "public/" public-dir)))

