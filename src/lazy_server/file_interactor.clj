(ns lazy-server.file-interactor
  (:require [clojure.string :refer [trim]]
            [clojure.java.io :refer [input-stream writer]]))

(defn read-file [file]
  (with-open [reader (input-stream file)]
    (let [buffer (byte-array (.length file))]
      (.read reader buffer)
      (bytes buffer))))

(defn write-to-file [path content]
  (with-open [w (writer path)]
    (.write w content)))
