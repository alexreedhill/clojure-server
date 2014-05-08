(ns clojure-server.file-reader-spec
  (:require [clojure-server.file-reader :refer :all])
  (:require [speclj.core :refer :all]))

(describe "file reader"
  (it "reads plain text file into byte array"
    (let [file-bytes (read-file "./spec/public/foo.txt")]
    (should= "class [B" (str (type file-bytes)))
    (should= "foo" (apply str (map char file-bytes))))))
