(ns lazy-server.basic-authenticator-spec
  (:require [lazy-server.basic-authenticator :refer :all]
            [speclj.core :refer :all]
            [clojure.data.codec.base64 :as base64]))

(describe "basic authenticator"

  (it "returns authentication required response map for restricted resource"
    (should= {:code 401
              :body "Authentication Required"
              :headers {"WWW-Authenticate" "Basic realm=lazy-server"}}
      (basic-auth {:headers {}} "Authentication Required" "Success!" "foo:bar")))

  (it "allows request to resolve if correct credentials are received"
    (let [creds (apply str (map char (base64/encode (.getBytes "foo:bar"))))
          request {:headers {"Authorization" (str "Basic " creds)}}]
      (should= {:code 200 :body "Success!"}
        (basic-auth request "Authentication Required" "Success!" "foo:bar"))))

  (it "returns authentication required response for incorrect credentials"
    (should= {:code 401
              :body "Authentication Required"
              :headers {"WWW-Authenticate" "Basic realm=lazy-server"}}
      (let [creds (apply str (map char (base64/encode (.getBytes "incorrect:creds"))))
            request {:headers {"Authorization" (str "Basic " creds)}}]
        (basic-auth request "Authentication Required" "Success!" "correct:creds")))))
