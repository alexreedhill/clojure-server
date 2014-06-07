(ns lazy-server.router-spec
  (:require [lazy-server.router :refer :all]
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
        (get-router {:method "GET" :path "/"})))

    (it "routes resource request"
      (should= "HTTP/1.1 200 OK\r\n\n/resource response body"
        (get-router {:method "GET" :path "/resource"})))

    (it "doesn't route unkown method"
      (should= "HTTP/1.1 404 Not Found\r\n\nSorry, there's nothing here!"
        (get-router {:method "POST" :path "/"})))

    (it "doesn't route unkown path"
      (should= "HTTP/1.1 404 Not Found\r\n\nSorry, there's nothing here!"
        (get-router {:method "GET" :path "/foobar"})))))
