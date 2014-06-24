(ns lazy-server.router-spec
  (:require [lazy-server.router :refer :all]
            [lazy-server.response-builder :refer [build]]
            [lazy-server.file-interactor :refer :all]
            [lazy-server.spec-helper :refer [bytes-to-string]]
            [clojure.java.io :refer [delete-file]]
            [digest :refer [sha1]]
            [speclj.core :refer :all]))

(describe "router"
  (context "get"
    (before-all
      (defn get-response-body [request]
        (str (request :path) " response body"))

      (defrouter get-router request
        (GET "/" {:code 200 :body (lazy-server.router-spec/get-response-body request)})
        (GET "/resource" {:code 200 :body (lazy-server.router-spec/get-response-body request)})
        (not-found "Sorry, there's nothing here!")))

    (it "routes root request"
      (should= "HTTP/1.1 200 OK\r\n\n/ response body"
        (bytes-to-string (get-router {:method "GET" :path "/"}))))

    (it "routes resource request"
      (should= "HTTP/1.1 200 OK\r\n\n/resource response body"
        (bytes-to-string (get-router {:method "GET" :path "/resource"}))))

    (it "doesn't route unkown path"
      (should= "HTTP/1.1 404 Not Found\r\n\nSorry, there's nothing here!"
        (bytes-to-string (get-router {:method "GET" :path "/foobar"}))))

    (context "public file"
      (it "routes to public file by default if get request of same path"
        (with-redefs [file-exists? (fn [_] true)
                      read-file (fn [_] (.getBytes "file1 contents\n"))]
          (should= "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\nfile1 contents\n"
            (bytes-to-string (get-router {:method "GET" :path "/file1.txt"})))))

      (it "returns method not allowed for public file if request method is not get"
        (with-redefs [file-exists? (fn [_] true)]
          (should= "HTTP/1.1 405 Method Not Allowed\r\nAllow: GET\r\n\n"
            (bytes-to-string (get-router {:method "PUT" :path "/file1.txt"})))))))

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
        (bytes-to-string (put-router {:method "PUT" :path "/form" :body "foo"})))))

  (context "patch"
    (with sha1-default (sha1 "default content\n"))
    (with sha1-patched (sha1 "patched content"))

    (before-all
      (write-to-file "public/patch-content.txt" "default content")
      (defrouter patch-router request
        (PATCH "/patch-content.txt" {:code 204})))

    (it "routes patch request with correct if-match etag"
      (with-redefs [read-file (fn [_] "default content\n")]
        (should= (str "HTTP/1.1 204 No Content\r\nEtag: " @sha1-patched "\r\n\n")
          (bytes-to-string (patch-router
                             {:method "PATCH"
                              :headers {"If-Match" @sha1-default}
                              :path "/patch-content.txt"
                              :body "patched content"})))))

    (it "routes patch request with incorrect if-match etag"
      (with-redefs [read-file (fn [_] "default content\n")]
        (should= (str "HTTP/1.1 412 Precondition Failed\r\nEtag: " @sha1-default "\r\n\n")
          (bytes-to-string (patch-router
                           {:method "PATCH"
                            :headers {"If-Match" "incorrect etag"}
                            :path "/patch-content.txt"
                            :body "patched content"})))))

    (it "calls patch response function on successful patch"
      (defrouter patch-save-router request
        (PATCH "/patch-content.txt" (save-resource request)))
      (write-to-file "public/patch-content.txt" "default content")
      (patch-save-router {:method "PATCH"
                          :path "/patch-content.txt"
                          :headers {"If-Match" @sha1-default}
                          :body "patched content"})
      (should= "patched content\n"
        (bytes-to-string (read-file "public/patch-content.txt")))
      (delete-file "public/patch-content.txt")))

  (context "save resource"
    (it "success"
      (with-redefs [write-to-file (fn [_ _] true)]
        (should= {:code 200}
          (save-resource {:path "/form" :body "data = cosby"}))))

    (it "failure"
      (with-redefs [write-to-file (fn [_ _] false)]
        (should= {:code 500}
          (save-resource {:path "/form" :body "data = heathcliff"})))))

  (context "serve file"
    (before-all
      (defrouter serve-router request
        (GET "/file1.txt" (serve-file request))))

    (it "serves file successfully"
      (with-redefs [read-file (fn [_] "file1 contents")]
        (let [request {:path "/file1.txt" :method "GET" :headers {}}]
          (should= "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\nfile1 contents"
            (bytes-to-string (serve-router request))))))

    (it "serves partial content"
      (with-redefs [read-partial-file (fn [_ _ _] "test")]
        (let [request {:method "GET" :path "/file1.txt" :headers {"Range" "bytes=0-4"}}]
          (should= "HTTP/1.1 206 Partial Content\r\nContent-Type: text/plain\r\n\ntest"
            (bytes-to-string (serve-router request))))))

    (it "fails to serve a file"
      (with-redefs [read-file (fn [_] nil)]
        (let [request {:method "GET" :path "/file1.txt" :headers {}}]
          (should= "HTTP/1.1 404 Not Found\r\n\n"
            (bytes-to-string (serve-router request))))))

    (it "doesn't require a request to have headers in order to serve file"
      (with-redefs [read-file (fn [_] "file1 contents")]
        (let [request {:method "GET" :path "/file1.txt"}]
          (should= "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\nfile1 contents"
            (bytes-to-string (serve-router request)))))))

  (context "method not allowed"
    (it "client error routes method not allowed"
      (should= "HTTP/1.1 405 Method Not Allowed\r\nAllow: GET\r\n\n"
        (bytes-to-string (client-error {:method "POST" :path "/"} request ((GET "/" {:code 200}))))))

    (context "not found"
      (before-all
        (defrouter not-allowed-router request
          (GET "/" {:code 200})
          (POST "/" {:code 200})
          (not-found "Sorry, there's nothing here!")))

      (it "routes method not allowed with not-found defined"
        (should= "HTTP/1.1 405 Method Not Allowed\r\nAllow: GET,POST\r\n\n"
          (bytes-to-string (not-allowed-router {:method "PUT" :path "/"}))))

      (it "routes to not-found if no path or method matches request"
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
        (should= false (request-matches? {:path "/" :method "POST"} "/foobar" "POST"))))

    (context "if-match"
      (with sha1-default (sha1 "default content"))

      (it "matches if match header to resource"
          (should= true
            (if-match-header-matches? {:path "/patch-content.txt"
                                       :headers {"If-Match" @sha1-default}}
                                      "default content")))

      (it "doesn't match incorrect if match header"
          (should= false
            (if-match-header-matches? {:path "/patch-content.txt"
                                       :headers {"If-Match" "foo"}}
                                      "default content")))))

  (context "not-found?"
    (it "determines not found is false when only one route is defined"
      (let [routes '((GET "/" {:code 200}))]
        (should= false (not-found? routes))))

    (it "determines not-found is false with more than one route left"
      (let [routes '((GET "/" {:code 200}) (POST "/" {:code 200}))]
        (should= false (not-found? routes))))

    (it "determines not-found is true if only not found route is left"
      (let [routes '((not-found "Sorry, there's nothing here!"))]
        (should= true (not-found? routes))))))

