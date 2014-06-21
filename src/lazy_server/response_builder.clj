(ns lazy-server.response-builder
  (:require [clojure.string :refer [trim-newline]]))

(def status-messages
  {200 "OK"
   204 "No Content"
   206 "Partial Content"
   301 "Moved Permanently"
   401 "Unauthorized"
   404 "Not Found"
   405 "Method Not Allowed"
   412 "Precondition Failed"
   500 "Internal Server Error"})

(defn build-status-line [response]
  (.getBytes
    (str "HTTP/1.1 "
         (response :code) " "
         (status-messages (response :code)))))

(defn build-headers [response]
  (let [headers (response :headers)]
    (if (> (count headers) 0)
      (->> headers
           (map #(str (key %) ": " (val %) "\n"))
           (apply str)
           (trim-newline)
           (str "\r\n")
           (.getBytes)))))

(defn build-body [response]
  (try
    (.getBytes (response :body))
    (catch IllegalArgumentException e (response :body))
    (catch NullPointerException e (byte-array 0))))

(defn build [request response]
  (let [status-line (build-status-line response)
        headers (build-headers response)
        body (build-body response)]
    (->> [status-line headers (.getBytes "\r\n\n") body]
         (mapcat seq)
         (byte-array))))
