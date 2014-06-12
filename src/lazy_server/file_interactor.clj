(ns lazy-server.file-interactor
  (:require [clojure.string :refer [trim split blank?]]
            [clojure.java.io :refer [input-stream writer]]))

(defn read-entire-file [file]
  (with-open [reader (input-stream file)]
    (let [buffer (byte-array (.length file))]
      (.read reader buffer)
      buffer)))

(defn read-partial-file [file range-header]
  (let [range (second (split range-header #"="))
        [min max] (map read-string (split range #"-"))
        buffer (byte-array (- max min))]
    (with-open [reader (input-stream file)]
      (.read reader buffer min (- max min))
      buffer)))

(defn write-to-file [path content]
  (with-open [w (writer path)]
    (.write w content)))
