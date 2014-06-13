(ns lazy-server.response-builder-spec
  (:require [lazy-server.response-builder :refer :all]
            [lazy-server.file-interactor :refer [read-file read-partial-file]]
            [lazy-server.spec-helper :refer [bytes-to-string]]
            [speclj.core :refer :all]))

(describe "response builder"
  (it "builds 200 OK response"
    (should= "HTTP/1.1 200 OK\r\n\n"
      (bytes-to-string (build {:path "/"} {:code 200 :body ""}))))

  (it "builds 404 Not Found response"
    (should= "HTTP/1.1 404 Not Found\r\n\nOops! There's nothing here"
      (bytes-to-string (build {:path "/foo"} {:code 404 :body "Oops! There's nothing here"}))))

  (it "builds response with headers"
    (should= "HTTP/1.1 200 OK\r\nBoo: Far\nFoo: Bar\r\n\nbody"
      (bytes-to-string (build {:path "/"} {:code 200 :headers {"Foo" "Bar" "Boo" "Far"} :body "body"}))))

  (it "builds redirect response"
    (should= "HTTP/1.1 301 Moved Permanently\r\nLocation: /\r\n\n"
      (bytes-to-string (build {:path "/redirect" } (redirect "/")))))

  (context "serve file"
    (it "builds sucessful file contents response"
      (with-redefs [read-file (fn [path] "file1 contents")]
        (let [request {:path "/file1.txt" :headers {}}]
          (should= "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\nfile1 contents"
            (bytes-to-string (build request (serve-file request)))))))

    (it "builds unsucessful file contents response"
      (with-redefs [read-file (fn [path] nil)]
        (let [request {:path "/file1.txt" :headers {}}]
          (should= "HTTP/1.1 404 Not Found\r\n\n"
            (bytes-to-string (build request (serve-file request)))))))

    (it "builds partial content response"
      (with-redefs [read-partial-file (fn [path range-header] "test")]
        (let [request {:path "/file1.txt" :headers {"Range" "bytes=0-4"}}]
          (should= "HTTP/1.1 206 Partial Content\r\nContent-Type: text/plain\r\n\ntest"
            (bytes-to-string (build request (serve-file request))))))))

  (it "builds method not allowed response"
    (should= {:code 405 :headers {"Allow" "GET,POST"}}
      (method-not-allowed-response ['GET 'POST]))))

