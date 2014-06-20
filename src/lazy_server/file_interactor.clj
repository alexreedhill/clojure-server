(ns lazy-server.file-interactor
  (:require [clojure.string :refer [trim split blank?]]
            [clojure.java.io :refer [output-stream input-stream as-file]]))

(defn read-file [file-path]
  (with-open [reader (input-stream file-path)]
    (let [length (.length (as-file file-path))
          buffer (byte-array length)]
      (.read reader buffer 0 length)
      buffer)))

(defn read-partial-file [file-path min max]
  (with-open [reader (input-stream file-path)]
    (let [length (- max min)
          buffer (byte-array length)]
      (.read reader buffer min length)
      buffer)))

(defn write-to-file [path content]
  (try
    (with-open [w (output-stream path)]
      (let [content-newline (byte-array (mapcat seq [(.getBytes content) (.getBytes "\n")]))]
        (.write w content-newline)))
    true
    (catch Exception e
      (do (println e) false))))

(defn log-request [request path]
  (spit path (str
               (request :method) " "
               (request :path) " "
               (request :http-version) "\n") :append true))

(defn file-exists? [path]
  (let [file (as-file path)]
    (and
      (.exists file)
      (not (.isDirectory file)))))

