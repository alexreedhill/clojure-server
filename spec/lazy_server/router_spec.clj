(ns lazy-server.router-spec
  (:require [lazy-server.router :refer :all]
            [lazy-server.spec-helper :refer [bytes-to-string]]
            [speclj.core :refer :all]))

(describe "router"
  (context "get"
    (before-all
      (defn get-response-body [request]
        (str (request :path) " response body"))

      (defrouter get-router request
        (GET "/" {:code 200 :body (get-response-body request)})
        (GET "/resource" {:code 200 :body (get-response-body request)})
        (four-oh-four "Sorry, there's nothing here!")))

    (it "routes root request"
      (should= "HTTP/1.1 200 OK\r\n\n/ response body"
        (bytes-to-string (get-router {:method "GET" :path "/"}))))

    (it "routes resource request"
      (should= "HTTP/1.1 200 OK\r\n\n/resource response body"
        (bytes-to-string (get-router {:method "GET" :path "/resource"}))))

    (it "doesn't route unkown method"
      (should= "HTTP/1.1 404 Not Found\r\n\nSorry, there's nothing here!"
        (bytes-to-string (get-router {:method "POST" :path "/"}))))

    (it "doesn't route unkown path"
      (should= "HTTP/1.1 404 Not Found\r\n\nSorry, there's nothing here!"
        (bytes-to-string (get-router {:method "GET" :path "/foobar"})))))

  (context "post"
    (before-all
      (defrouter post-router request
        (POST "/form" {:code 200})))

    (it "routes post request"
      (should= "HTTP/1.1 200 OK\r\n\n"
        (bytes-to-string (post-router {:method "POST" :path "/form"})))))

  (context "options"
    (before-all
      (defrouter options-router request
        (OPTIONS "/method_options" {:code 200})))

    (it "routes options request"
      (should= "HTTP/1.1 200 OK\r\nAllow: GET,HEAD,POST,OPTIONS,PUT\r\n\n"
        (bytes-to-string (options-router {:method "OPTIONS" :path "/method_options"})))))

  (context "put"
    (before-all
      (defrouter put-router request
        (PUT "/form" {:code 200})))

    (it "routes put request"
      (should= "HTTP/1.1 200 OK\r\n\n"
        (bytes-to-string (put-router {:method "PUT" :path "/form"}))))))

