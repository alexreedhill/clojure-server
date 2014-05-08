(ns clojure-server.server-spec
  (:require [clojure-server.server :refer :all])
  (:require [speclj.core :refer :all])
  (:require [clojure.java.io :refer [writer]])
  (:import (java.net Socket ServerSocket InetAddress ConnectException)))

(defn connect-socket [port address]
  (try
    (Socket. address port)
    (catch ConnectException e
      (connect-socket port address))))

(defn send-mock-request [request]
  (with-open [writer (writer (connect-socket 5000 "localhost"))]
      (.write writer request)))

(describe "opens sockets"
  (it "opens a java.net.ServerSocket on the correct port and address"
    (with-open [server-socket (open-server-socket 5000 "localhost")]
      (should= ServerSocket (class server-socket))
      (should= (.getLocalPort server-socket) 5000)
      (should= (.getInetAddress server-socket) (InetAddress/getByName "localhost"))))

  (it "listens for and opens socket on incoming request"
    (with-open [server-socket (open-server-socket 5000 "localhost")]
      (let [sockets (doall
                      (pvalues (listen server-socket)
                               (connect-socket 5000 "localhost")))]
        (should= (.getLocalPort (first sockets))
          (.getPort (second sockets)))
        (should= (.getPort (first sockets))
          (.getLocalPort (second sockets)))))))

(describe "reads request"
  (it "creates line sequence from client input"
    (with-open [server-socket (open-server-socket 5000 "localhost")]
      (let [mock-request (first (doall
                                  (pvalues (read-request (listen server-socket))
                                           (send-mock-request "foo"))))]
        (.close server-socket)
        (should= '("foo") mock-request))))

  (it "reads multiline request"
    (with-open [server-socket (open-server-socket 5000 "localhost")]
      (let [mock-request (first (doall
                                  (pvalues (read-request (listen server-socket))
                                           (send-mock-request
                                             "GET / HTTP/1.1\r\nFoo: Bar"))))]
        (.close server-socket)
        (should= '("GET / HTTP/1.1" "Foo: Bar") mock-request)))))


