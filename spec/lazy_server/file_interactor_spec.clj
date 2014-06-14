(ns lazy-server.file-interactor-spec
  (:require [lazy-server.file-interactor :refer :all]
            [lazy-server.spec-helper :refer [bytes-to-string]]
            [clojure.string :refer [trim]]
            [clojure.java.io :refer [writer delete-file]]
            [speclj.core :refer :all]))

(describe "file interactor"
  (before-all
    (write-to-file "resources/test-file.txt" "test content"))

  (after-all
    (delete-file "resources/test-file.txt"))

  (it "writes to file and reads it"
    (let [file-contents (bytes-to-string (read-file "resources/test-file.txt"))]
      (should= "test content" (trim file-contents))))

  (it "reads partial file contents"
    (let [file-contents (bytes-to-string (read-partial-file "resources/test-file.txt" 0 4))]
      (should= "test" (trim file-contents))))

  (context "logs"
    (with request {:method "GET" :path "/" :http-version "HTTP/1.1"})
    (with test-path "public/test-log.txt")

    (after
      (delete-file @test-path))

    (it "logs requests"
      (log-request @request @test-path)
      (should= "GET / HTTP/1.1\n"
        (slurp @test-path)))

    (it "appends requests to log"
      (dotimes [n 2]
        (log-request @request @test-path))
      (should= "GET / HTTP/1.1\nGET / HTTP/1.1\n"
        (slurp @test-path)))))
