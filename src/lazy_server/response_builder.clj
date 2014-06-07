(ns lazy-server.response-builder
  (:require [lazy-server.file-interactor :refer [write-to-file]]
            [clojure.string :refer [join]]))

(def status-messages
  {200 "OK"
   404 "Not Found"})

(defn options-response [response]
  (assoc response :headers {"Allow" "GET,HEAD,POST,OPTIONS,PUT"}))

(defn build-header-string [headers]
  (let [header-string (str (first (first headers)) ": " (second (first headers)))]
    (if (< 1 (count headers))
      (str header-string "\n")
      header-string)))

(defn build-headers [response]
  (loop [headers (response :headers)
         acc                      ""]
    (if (= (count headers) 0)
      acc
      (recur (rest headers)
             (str acc (build-header-string headers))))))

(defn build [request response]
  (join "\r\n\n"
        [(str "HTTP/1.1 "
              (response :code) " "
              (status-messages (response :code))
              (if (> (count (response :headers)) 0)
                (str "\r\n" (build-headers response))))
         (response :body)]))
