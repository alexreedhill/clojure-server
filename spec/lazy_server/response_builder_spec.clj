(ns lazy-server.response-builder-spec
  (:require [lazy-server.response-builder :refer :all]
            [speclj.core :refer :all]))

(describe "response builder"

  (it "builds 200 OK response"
    (should= (build {:path "/"} {:code 200 :body ""})
      "HTTP/1.1 200 OK\r\n\n"))

  (it "builds 404 Not Found response"
    (should= (build {:path "/foo"} {:code 404 :body "Oops! There's nothing here"})
                    "HTTP/1.1 404 Not Found\r\n\nOops! There's nothing here")))
