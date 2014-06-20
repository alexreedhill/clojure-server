(ns lazy-server.response-builder-spec
  (:require [lazy-server.response-builder :refer :all]
            [lazy-server.file-interactor :refer [read-file read-partial-file write-to-file]]
            [lazy-server.spec-helper :refer [bytes-to-string]]
            [speclj.core :refer :all]))

(describe "response builder"
  (it "builds 200 OK response"
    (should= "HTTP/1.1 200 OK\r\n\n"
      (bytes-to-string (build {:path "/"} {:code 200 :body ""}))))

  (it "builds response with headers"
    (should= "HTTP/1.1 200 OK\r\nBoo: Far\nFoo: Bar\r\n\nbody"
      (bytes-to-string (build {:path "/"} {:code 200 :headers {"Foo" "Bar" "Boo" "Far"} :body "body"}))))

  (it "builds no content response"
    (should= "HTTP/1.1 204 No Content\r\n\n"
      (bytes-to-string (build {:path "/"} {:code 204}))))

  (it "builds redirect response"
    (should= "HTTP/1.1 301 Moved Permanently\r\nLocation: /\r\n\n"
      (bytes-to-string (build {:path "/redirect" } (redirect "/")))))

  (it "builds 404 Not Found response"
    (should= "HTTP/1.1 404 Not Found\r\n\nOops! There's nothing here"
      (bytes-to-string (build {:path "/foo"} {:code 404 :body "Oops! There's nothing here"}))))

  (it "builds unauthorized response"
    (should= "HTTP/1.1 401 Unauthorized\r\n\n"
      (bytes-to-string (build {:path "/restricted"} {:code 401}))))

  (it "builds precondition failed response"
    (should= "HTTP/1.1 412 Precondition Failed\r\n\n"
      (bytes-to-string (build {:path "/patch"} {:code 412}))))

  (it "builds internal server error response"
    (should= "HTTP/1.1 500 Internal Server Error\r\n\n"
      (bytes-to-string (build {:path "/error"} {:code 500}))))

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
      (with-redefs [read-partial-file (fn [path min max] "test")]
        (let [request {:path "/file1.txt" :headers {"Range" "bytes=0-4"}}]
          (should= "HTTP/1.1 206 Partial Content\r\nContent-Type: text/plain\r\n\ntest"
            (bytes-to-string (build request (serve-file request)))))))

    (it "doesn't require a request to have headers in order to serve file"
      (with-redefs [read-file (fn [path] "file1 contents")]
        (let [request {:path "/file1.txt"}]
          (should= "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\nfile1 contents"
            (bytes-to-string (build request (serve-file request))))))))

  (it "builds method not allowed response"
    (should= {:code 405 :headers {"Allow" "GET,POST"}}
      (method-not-allowed-response ['GET 'POST])))

  (context "save resource"
    (it "returns successful response"
      (with-redefs [write-to-file (fn [path contents] true)]
        (should= {:code 200}
          (save-resource {:path "/form" :body "data = cosby"}))))

    (it "returns unsuccessful response"
      (with-redefs [write-to-file (fn [path contents] false)]
        (should= {:code 500}
          (save-resource {:path "/form" :body "data = heathcliff"}))))))

