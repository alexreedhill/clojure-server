(ns lazy-server.router-spec
  (:require [lazy-server.router :refer :all]
            [speclj.core :refer :all]))

(describe "router"
  (context "get"
    (before-all
      (defrouter get-router request
        {:method "GET" :path "/" :response {:body "root response body" :code 200}}
        {:method "GET" :path "/resource" :response {:body "resource body" :code 200}}))

      (it "routes root request"
        (should= {:body "root response body" :code 200} (get-router {:method "GET" :path "/"})))

      (it "routes resource request"
        (should= {:body "resource body" :code 200} (get-router {:method "GET" :path "/resource"})))

      (it "doesn't route unkown method"
        (should= {:body nil :code 404} (get-router {:method "POST" :path "/"})))

      (it "doesn't route unkown path"
        (should= {:body nil :code 404} (get-router {:method "GET" :path "/foobar"})))))

