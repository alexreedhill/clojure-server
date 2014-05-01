(ns clojure-server.request-spec
  (:require [clojure-server.request :refer :all])
  (:require [speclj.core :refer :all]))

(describe "request"
  (it "parses request method"
      (should= "GET" (get (parse "GET / HTTP/1.1") :method)))

  (it "parses request path"
      (should= "/" (get (parse "GET / HTTP/1.1") :path)))

  (it "parses request http version"
      (should= "HTTP/1.1" (get (parse "GET / HTTP/1.1") :http-version))))
