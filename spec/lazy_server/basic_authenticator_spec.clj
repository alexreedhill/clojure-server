(ns lazy-server.basic-authenticator-spec
  (:require [lazy-server.basic-authenticator :refer :all]
            [lazy-server.spec-helper :refer [bytes-to-string]]
            [speclj.core :refer :all]
            [clojure.data.codec.base64 :as base64]))

(defn encode-creds [creds]
  (->> (.getBytes creds)
       (base64/encode)
       (bytes-to-string)))

(describe "basic authenticator"
  (it "returns authentication required response map for restricted resource"
    (should= {:code 401
              :body "Authentication Required"
              :headers {"WWW-Authenticate" "Basic realm=lazy-server"}}
      (authenticate {:headers {}} "Authentication Required" "Success!" "foo:bar")))

  (it "allows request to resolve if correct credentials are received"
    (let [creds (encode-creds "foo:bar")
          request {:headers {"Authorization" (str "Basic " creds)}}]
      (should= {:code 200 :body "Success!"}
        (authenticate request "Authentication Required" "Success!" "foo:bar"))))

  (it "returns authentication required response for incorrect credentials"
    (should= {:code 401
              :body "Authentication Required"
              :headers {"WWW-Authenticate" "Basic realm=lazy-server"}}
      (let [creds (encode-creds "foo:bar")
            request {:headers {"Authorization" (str "Basic " creds)}}]
        (authenticate request "Authentication Required" "Success!" "correct:creds")))))
