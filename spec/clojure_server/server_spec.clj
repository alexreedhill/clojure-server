(ns clojure-server.server-spec
  (:require [clojure-server.server :refer :all])
  (:require [clojure.java.io :refer [reader writer]])
  (:require [speclj.core :refer :all])
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

  (it "reads line from client input"
    (should= "foo" (request-response "foo\n"))))

