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
        (should= "200 OK HTTP/1.1\r\n\nroot response body" (get-router {:method "GET" :path "/"})))

      (it "routes resource request"
        (should= "200 OK HTTP/1.1\r\n\nresource body" (get-router {:method "GET" :path "/resource"})))

      (it "doesn't route unkown method"
        (should= "404 Not Found HTTP/1.1\r\n\n" (get-router {:method "POST" :path "/"})))

      (it "doesn't route unkown path"
        (should= "404 Not Found HTTP/1.1\r\n\n" (get-router {:method "GET" :path "/foobar"})))))
