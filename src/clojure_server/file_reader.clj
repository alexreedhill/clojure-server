(ns clojure-server.file-reader
  (:require [clojure.string :refer [trim]]))

(defn read-file [path]
  (bytes (byte-array (map (comp byte int) (trim (slurp path))))))
