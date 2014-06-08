(ns lazy-server.response-builder
  (:require [lazy-server.file-interactor :refer [read-file write-to-file]]))

(def status-messages
  {200 "OK"
   301 "Moved Permanently"
   404 "Not Found"})

(defn redirect [path]
  {:code 301 :headers {"Location" path}})

(defn options-response [response]
  (assoc response :headers {"Allow" "GET,HEAD,POST,OPTIONS,PUT"}))

(defn serve-file [request]
  (let [file-contents (read-file (request :path))]
    (if (nil? file-contents)
      {:code 404}
      {:code 200 :body file-contents})))

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
  (str "\r\n\n" (response :body)))

(defn build [request response]
  (str
    (build-status-line response)
    (build-headers response)
    (build-body response)))
