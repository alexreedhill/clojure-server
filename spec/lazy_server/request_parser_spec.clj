(ns lazy-server.request-parser-spec
  (:require [lazy-server.request-parser :refer :all]
            [speclj.core :refer :all]))

(describe "request parser"
  (it "parses request method"
    (should= "GET" ((parse ["GET / HTTP/1.1"]) :method)))

  (it "parses request path"
    (should= "/" ((parse ["GET / HTTP/1.1"]) :path)))

  (it "parses request http version"
    (should= "HTTP/1.1" ((parse ["GET / HTTP/1.1"]) :http-version)))

  (it "parses query string parameters"
    (should= {:foo "bar"} ((parse ["GET /?foo=bar HTTP/1.1"]) :query-params)))

  (it "parses a single header"
    (should= {:foo "Bar"} ((parse ["GET / HTTP/1.1" "Foo: Bar"]) :headers)))

  (it "parses multiple headers"
    (should= {:foo "Bar" :boo "Far"}
      ((parse ["GET / HTTP/1.1" "Foo: Bar" "Boo: Far"]) :headers)))

  (it "decodes query string parameters with parameter decoder"
    (should= {:foo "bar"} ((parse ["GET /?foo%3Dbar HTTP/1.1"]) :query-params))))
