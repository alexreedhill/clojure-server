(ns lazy-server.file-interactor-spec
  (:require [lazy-server.file-interactor :refer :all]
            [lazy-server.spec-helper :refer [bytes-to-string]]
            [clojure.string :refer [trim]]
            [clojure.java.io :refer [writer delete-file]]
            [speclj.core :refer :all]))

(describe "file interactor"
  (with test-file "resources/test-file.txt")

  (before-all
    (write-to-file @test-file "test content"))

  (after-all
    (delete-file @test-file))

  (it "writes to file and reads it"
    (let [file-contents (bytes-to-string (read-file @test-file))]
      (should= "test content" (trim file-contents))))

  (it "reads partial file contents"
    (let [file-contents (bytes-to-string (read-partial-file @test-file 0 4))]
      (should= "test" (trim file-contents))))

  (context "logs"
    (with request {:method "GET" :path "/" :http-version "HTTP/1.1"})
    (with test-log "public/test-log.txt")

    (after
      (delete-file @test-log))

    (it "logs requests"
      (log-request @request @test-log)
      (should= "GET / HTTP/1.1\n"
        (slurp @test-log)))

    (it "appends requests to log"
      (dotimes [n 2]
        (log-request @request @test-log))
      (should= "GET / HTTP/1.1\nGET / HTTP/1.1\n"
        (slurp @test-log))))

  (context "file-exists?"
    (with test-file "public/test-file.txt")

    (before-all
      (write-to-file @test-file "test content"))

    (after-all
      (delete-file @test-file))

    (it "recognizes file exists"
      (should= true
        (file-exists? @test-file)))

    (it "recognizes file doesn't exist with incorrect path"
      (should= false
        (file-exists? "public/foobar.txt")))

    (it "recognizes file doesn't exists if path points to directory"
      (should= false
        (file-exists? "public/")))))

