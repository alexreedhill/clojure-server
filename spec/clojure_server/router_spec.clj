(ns clojure-server.router-spec
  (:require [clojure-server.router :refer :all]
            [speclj.core :refer :all]))

(describe "router"
  (context "get"
    (before-all
      (defrouter get-router request
        (get "/" ["root response body" 200])
        (get "/resource" ["resource body", 200])))

      (it "routes root request"
        (should= ["root response body" 200] (get-router {:method "GET" :path "/"})))

      (it "routes resource request"
        (should= ["resource body" 200] (get-router {:method "GET" :path "/resource"})))

      (it "doesn't route post request"
        (should= [nil 404] (get-router {:method "POST" :path "/"})))))

