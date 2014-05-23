(ns lazy-server.file-reader
  (:require [clojure.string :refer [trim]]
            [clojure.contrib.io :refer [to-byte-array]]))

(defn read-file [path]
  (to-byte-array path))
