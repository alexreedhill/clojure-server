(ns lazy-server.file-interactor-spec
  (:require [lazy-server.file-interactor :refer :all]
            [clojure.string :refer [trim]]
            [clojure.java.io :refer [writer delete-file]]
            [speclj.core :refer :all]))

(describe "file interactor"
  (it "writes to file and reads it"
    (write-to-file "resources/test-file.txt" "test content")
    (let [file-contents (apply str (map char (read-file "resources/test-file.txt")))]
      (should= "test content" (trim file-contents)))
    (delete-file "resources/test-file.txt")
    (should-throw (read-file "resources/test-file.txt"))))
