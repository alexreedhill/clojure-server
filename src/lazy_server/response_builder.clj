(ns lazy-server.response-builder
  (:require [lazy-server.file-interactor :refer [read-file write-to-file]]
            [clojure.string :refer [join]]
            [pantomime.mime :refer [mime-type-of]])
  (import (java.lang IllegalArgumentException NullPointerException)))

(def status-messages
  {200 "OK"
   301 "Moved Permanently"
   404 "Not Found"
   405 "Method Not Allowed"})

(defn redirect [path]
  {:code 301 :headers {"Location" path}})

(defn options-response [response]
  (assoc response :headers {"Allow" "GET,HEAD,POST,OPTIONS,PUT"}))

(defn method-not-allowed-response [allowed]
  {:code 405 :headers {"Allow" (join "," allowed)}})

(defn serve-file [request]
  (let [file-contents (read-file (request :path) (get (request :headers) "Range" ""))]
    (if (nil? file-contents)
      {:code 404}
      {:code 200
       :headers {"Content-Type" (mime-type-of (request :path))}
       :body file-contents})))

(defn build-status-line [response]
  (str "HTTP/1.1 "
       (response :code) " "
       (status-messages (response :code))))

(defn build-header-string [headers]
  (let [header-string (str (first (first headers)) ": "
                           (second (first headers)))]
    (if (< 1 (count headers))
      (str header-string "\n")
      header-string)))

(defn build-headers [response]
  (if (> (count (response :headers)) 0)
    (loop [headers (response :headers)
           output               "\r\n"]
      (if (= (count headers) 0)
        output
        (recur (rest headers)
               (str output (build-header-string headers)))))))

(defn build-body [response]
  (try
    (.getBytes (response :body))
    (catch IllegalArgumentException e (response :body))
    (catch NullPointerException e (byte-array 0))))

(defn build [request response]
  (byte-array
    (mapcat seq
            [(.getBytes
               (str
                 (build-status-line response)
                 (build-headers response) "\r\n\n"))
            (build-body response)])))
