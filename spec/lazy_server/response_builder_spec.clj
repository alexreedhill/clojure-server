(ns lazy-server.response-builder-spec
  (:require [lazy-server.response-builder :refer :all]
            [speclj.core :refer :all]))

(describe "response builder"
  (it "builds 200 OK response"
    (should= "HTTP/1.1 200 OK\r\n\n"
      (build {:path "/"} {:code 200 :body ""})))

  (it "builds 404 Not Found response"
    (should= "HTTP/1.1 404 Not Found\r\n\nOops! There's nothing here"
      (build {:path "/foo"} {:code 404 :body "Oops! There's nothing here"})))

  (it "builds response with headers"
    (should= "HTTP/1.1 200 OK\r\nBoo: Far\nFoo: Bar\r\n\nbody"
      (build {:path "/"} {:code 200 :headers {"Foo" "Bar" "Boo" "Far"} :body "body"}))))
