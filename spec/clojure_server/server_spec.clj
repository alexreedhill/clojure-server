(ns clojure-server.server-spec
  (:require [clojure-server.server :refer :all])
  (:require [speclj.core :refer :all])
  (:require [clojure.java.io :refer [writer]])
  (:import (java.net Socket ServerSocket InetAddress ConnectException)))

(defn connect-socket [port address]
  (try
    (Socket. address port)
    (catch ConnectException e
      (connect-socket address port))))

(defn send-mock-request []
    (with-open [writer (writer (connect-socket 5000 "localhost"))]
      (.write writer "foo")))

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
      (let [mock-request (first (doall
                           (pvalues (run 5000 "localhost")
                                    (send-mock-request))))]
        (should= '("foo") mock-request))))

