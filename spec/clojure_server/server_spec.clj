(ns clojure-server.server-spec
  (:require [clojure-server.server :refer :all])
  (:require [speclj.core :refer :all])
  (:import (java.net Socket ServerSocket InetAddress)))

(defn mock-incoming-request [address port]
  (try
    (Socket. address port)
    (catch java.net.ConnectException e
      (mock-incoming-request address port))))

(describe "open server socket"
  (it "opens a java.net.ServerSocket on the correct port and address"
    (with-open [server-socket (open-server-socket 5000 "localhost")]
      (should= ServerSocket (class server-socket))
      (should= (.getLocalPort server-socket) 5000)
      (should= (.getInetAddress server-socket) (InetAddress/getByName "localhost"))))

  (it "listens for and opens socket on incoming request"
      (with-open [server-socket (open-server-socket 5000 "localhost")]
        (let [sockets (doall
                        (pvalues (listen server-socket)
                                 (mock-incoming-request "localhost" 5000)))]
        (should= (.getLocalPort (first sockets))
                 (.getPort (second sockets)))))))

