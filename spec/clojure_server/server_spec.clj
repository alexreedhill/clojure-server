(ns clojure-server.server-spec
  (:require [clojure-server.server :refer :all])
  (:require [speclj.core :refer :all]))

(describe "open server socket"
  (it "opens a java.net.ServerSocket on the correct port and address"
    (with-open [socket (open-server-socket 5000 "localhost")]
      (should= java.net.ServerSocket (class socket))
      (should= (.getLocalPort socket) 5000)
      (should= (.getInetAddress socket) (java.net.InetAddress/getByName "localhost")))))

