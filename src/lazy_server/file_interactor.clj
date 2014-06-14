(ns lazy-server.file-interactor
  (:require [clojure.string :refer [trim split blank?]]
            [clojure.java.io :refer [input-stream writer file]]))

(defn read-file [file-path]
  (with-open [reader (input-stream file-path)]
    (let [length (.length (file file-path))
          buffer (byte-array length)]
      (.read reader buffer 0 length)
      buffer)))

(defn read-partial-file [file-path min max]
  (let [length (- max min)
        buffer (byte-array length)]
    (with-open [reader (input-stream file-path)]
      (.read reader buffer min length)
      buffer)))

(defn write-to-file [path content]
  (with-open [w (writer path)]
    (.write w content)))

(defn log-request [request path]
  (spit path (str
               (request :method) " "
               (request :path) " "
               (request :http-version) "\n") :append true))
