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
        (bytes-to-string (put-router {:method "PUT" :path "/form"})))))

  (context "method not allowed"
    (it "method not allowed returns response"
      (should= "HTTP/1.1 405 Method Not Allowed\r\nAllow: GET\r\n\n"
        (bytes-to-string (method-not-allowed {:method "POST" :path "/"} request ((GET "/" {:code 200}))))))

    (it "routes method not allowed without four-oh-four defined"
      (defrouter not-allowed-router request
        (GET "/" {:code 200})
        (POST "/" {:code 200}))
      (should= "HTTP/1.1 405 Method Not Allowed\r\nAllow: GET,POST\r\n\n"
        (bytes-to-string (not-allowed-router {:method "PUT" :path "/"}))))

    (context "not found"
      (before-all
        (defrouter not-allowed-router request
          (GET "/" {:code 200})
          (POST "/" {:code 200})
          (four-oh-four "Sorry, there's nothing here!")))

      (it "routes method not allowed with four-oh-four defined"
        (should= "HTTP/1.1 405 Method Not Allowed\r\nAllow: GET,POST\r\n\n"
          (bytes-to-string (not-allowed-router {:method "PUT" :path "/"}))))

      (it "routes to four-oh-four if no path or method matches request"
        (should= "HTTP/1.1 404 Not Found\r\n\nSorry, there's nothing here!"
          (bytes-to-string (not-allowed-router {:method "GET" :path "/foobar"}))))))

  (context "matching"
    (context "path"
      (it "matches path"
        (should= true (path-matches? {:path "/"} "/")))

      (it "doesn't match different paths"
        (should= false (path-matches? {:path "/foobar"} "/"))))

    (context "method"
      (it "matches method"
        (should= true (method-matches? {:method "GET"} "GET")))

      (it "doesn't match different methods"
        (should= false (method-matches? {:method "PUT"} "POST"))))

    (context "request"
      (it "matches request with route"
        (should= true (request-matches? {:path "/" :method "GET"} "/" "GET")))

      (it "doesn't match request with route if different method"
        (should= false (request-matches? {:path "/" :method "POST"} "/" "PUT")))

      (it "doesn't match request with route if different path"
        (should= false (request-matches? {:path "/" :method "POST"} "/foobar" "POST")))))

  (context "last-route?"
    (it "determines last route is false when only one route is defined"
      (let [routes '(route)]
        (should= false (last-route? routes routes))))

    (it "determines last route is false with more than one route left"
      (let [routes '(route1 route2)]
        (should= false (last-route? routes routes))))

    (it "determines last route is true if there is only one route left"
      (let [routes '(route1 route2)]
        (should= true (last-route? (list (last routes)) routes))))))

