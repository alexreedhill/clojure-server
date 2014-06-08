(ns lazy-server.response-builder-spec
  (:require [lazy-server.response-builder :refer :all]
            [lazy-server.file-interactor :refer [read-file]]
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
      (build {:path "/"} {:code 200 :headers {"Foo" "Bar" "Boo" "Far"} :body "body"})))

  (it "builds redirect response"
    (should= "HTTP/1.1 301 Moved Permanently\r\nLocation: /\r\n\n"
      (build {:path "/redirect" } (redirect "/"))))

  (it "builds sucessful file contents response"
    (with-redefs [read-file (fn [path] "file1 contents")]
      (should= (str "HTTP/1.1 200 OK\r\n\nfile1 contents")
        (build {:path "/file1"} (serve-file {:path "/file1"})))))

  (it "builds unsucessful file contents response"
    (with-redefs [read-file (fn [path] nil)]
      (should= (str "HTTP/1.1 404 Not Found\r\n\n")
        (build {:path "/file1"} (serve-file {:path "/file1"}))))))
