(ns lazy-server.basic-authenticator
  (:require [clojure.data.codec.base64 :as base64]
            [clojure.string :refer [split]]))

(defn decode-client-creds [request]
  (if-let [auth-header (get (request :headers) "Authorization")]
    (-> (second (split auth-header #" "))
        (.getBytes)
        (base64/decode))))

(defn authenticate [request fail-body success-body creds]
  (let [client-creds (decode-client-creds request)]
    (if (= (apply str (map char client-creds)) creds)
      {:code 200 :body success-body}
      {:code 401 :body fail-body :headers {"WWW-Authenticate" "Basic realm=lazy-server"}})))
