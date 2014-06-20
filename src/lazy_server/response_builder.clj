(ns lazy-server.response-builder)

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
