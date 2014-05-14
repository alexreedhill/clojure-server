(ns clojure-server.file-reader
  (:require [clojure.string :refer [trim]]))

(defn my-threading [input & functions]
  (reduce #(%2 %1) input functions))

(defn string-to-byte-array [string]
  (byte-array (map (comp byte int) string)))

(defn read-file [path]
  (my-threading path slurp trim string-to-byte-array))


