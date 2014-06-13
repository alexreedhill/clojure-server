(ns lazy-server.file-interactor
  (:require [clojure.string :refer [trim split blank?]]
            [clojure.java.io :refer [input-stream writer file]]))


(defn read-file [file-path]
  (with-open [reader (input-stream file-path)]
    (let [file (file file-path)
          buffer (byte-array (.length file))]
      (.read reader buffer 0 (.length file))
      buffer)))

(defn read-partial-file [file-path range-header]
  (let [range (second (split range-header #"="))
        [min max] (map read-string (split range #"-"))
        buffer (byte-array (- max min))]
    (with-open [reader (input-stream file-path)]
      (.read reader buffer min (- max min))
      buffer)))

(defn write-to-file [path content]
  (with-open [w (writer path)]
    (.write w content)))
