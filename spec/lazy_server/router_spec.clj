(ns lazy-server.router-spec
  (:require [lazy-server.router :refer :all]
            [speclj.core :refer :all]))

(describe "router"
  (context "get"
    (before-all
      (defrouter get-router
        (GET "/" {:body "root response body" :code 200})
        (GET "/resource" {:body "resource body" :code 200})))

      (it "routes root request"
        (should= "HTTP/1.1 200 OK\r\n\nroot response body" (get-router {:method "GET" :path "/"})))

      (it "routes resource request"
        (should= "HTTP/1.1 200 OK\r\n\nresource body" (get-router {:method "GET" :path "/resource"})))

      (it "doesn't route unkown method"
        (should= "HTTP/1.1 404 Not Found\r\n\n" (get-router {:method "POST" :path "/"})))

      (it "doesn't route unkown path"
        (should= "HTTP/1.1 404 Not Found\r\n\n" (get-router {:method "GET" :path "/foobar"})))))
