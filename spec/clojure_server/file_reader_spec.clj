(ns clojure-server.file-reader-spec
  (:require [clojure-server.file-reader :refer :all])
  (:require [speclj.core :refer :all]))

(describe "file reader"
  (it "converts string into byte array"
    (let [byte-array (string-to-byte-array "foo")]
      (should-not= "foo" byte-array)
      (should= (String. byte-array) "foo")))

  (it "give the byte-array of the trimmed contents of a file"
    (let [file-contents "contents\n"
          trimmed  "contents"
          file-path "a path"
          expected-answer (string-to-byte-array trimmed)]
      (with-redefs [slurp {file-path file-contents}]
        (should= (seq expected-answer) (seq (read-file file-path))))
      )
    ))

