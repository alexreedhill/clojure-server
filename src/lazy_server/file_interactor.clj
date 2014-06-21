(ns lazy-server.file-interactor
  (:require [clojure.string :refer [trim split blank?]]
            [clojure.java.io :refer [output-stream input-stream as-file]]))

(defn read-file [path]
  (with-open [reader (input-stream path)]
    (let [length (.length (as-file path))
          buffer (byte-array length)]
      (.read reader buffer 0 length)
      buffer)))

(defn read-partial-file [path min max]
  (with-open [reader (input-stream path)]
    (let [length (- max min)
          buffer (byte-array length)]
      (.read reader buffer min length)
      buffer)))

(defn append-newline [content]
  (->> [content "\n"]
       (mapcat #(seq (.getBytes %)))
       (byte-array)))

(defn write-to-file [path content]
  (try
    (with-open [w (output-stream path)]
      (let [content-newline (append-newline content)]
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

(defn directory-contents [path]
  (->> (file-seq (as-file path))
       (map #(.getName %))
       (drop 1)))
