(ns lazy-server.response-builder-spec
  (:require [lazy-server.response-builder :refer :all]
            [lazy-server.file-interactor :refer [read-file read-partial-file write-to-file]]
            [lazy-server.spec-helper :refer [bytes-to-string]]
            [speclj.core :refer :all]))

(describe "response builder"
  (it "builds ok response"
    (should= "HTTP/1.1 200 OK\r\n\n"
      (bytes-to-string (build {} {:code 200 :body ""}))))

  (it "builds response with headers and body"
    (should= "HTTP/1.1 200 OK\r\nBoo: Far\nFoo: Bar\r\n\nbody"
      (bytes-to-string (build {} {:code 200 :headers {"Foo" "Bar" "Boo" "Far"} :body "body"}))))

  (it "builds no content response"
    (should= "HTTP/1.1 204 No Content\r\n\n"
      (bytes-to-string (build {} {:code 204}))))

  (it "builds redirect response"
    (should= "HTTP/1.1 301 Moved Permanently\r\nLocation: /\r\n\n"
      (bytes-to-string (build {} {:code 301 :headers {"Location" "/"}}))))

  (it "builds unauthorized response"
    (should= "HTTP/1.1 401 Unauthorized\r\n\n"
      (bytes-to-string (build {} {:code 401}))))

  (it "builds not found response"
    (should= "HTTP/1.1 404 Not Found\r\n\nOops! There's nothing here"
      (bytes-to-string (build {} {:code 404 :body "Oops! There's nothing here"}))))

  (it "builds method not allowed response"
    (should= "HTTP/1.1 405 Method Not Allowed\r\nAllow: GET,POST\r\n\n"
      (bytes-to-string (build {} {:code 405 :headers {"Allow" "GET,POST"}}))))

  (it "builds precondition failed response"
    (should= "HTTP/1.1 412 Precondition Failed\r\n\n"
      (bytes-to-string (build {} {:code 412}))))

  (it "builds internal server error response"
    (should= "HTTP/1.1 500 Internal Server Error\r\n\n"
      (bytes-to-string (build {} {:code 500})))))
