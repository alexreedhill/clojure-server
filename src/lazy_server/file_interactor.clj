(ns lazy-server.file-interactor
  (:require [clojure.string :refer [trim]]
            [clojure.contrib.io :refer [to-byte-array]]
            [clojure.java.io :refer [writer]]))

(defn read-file [path]
  (to-byte-array path))

(defn write-to-file [path content]
  (with-open [w (writer path)]
    (.write w content)))
