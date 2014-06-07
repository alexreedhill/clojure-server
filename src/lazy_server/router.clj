(ns lazy-server.router
  (:require [lazy-server.response-builder :refer [build]]))

(defmacro GET [path request-sym response]
  `(let [handler# (fn [~request-sym] (build ~request-sym ~response))]
     (fn [request#]
       (if (and (= ~path (request# :path)) (= (request# :method) "GET"))
         (handler# request#)))))

(defmacro four-oh-four [request-sym body]
  `(let [handler# (fn [~request-sym] (build ~request-sym {:code 404 :body ~body}))]
     (fn [request#] (handler# request#))))

(defmacro defrouter [router-name & routes]
  `(defn ~router-name [request#]
     (loop [routes# '~routes]
       (let [response# ((eval (first routes#)) request#)]
         (if (not (nil? response#))
           response#
           (recur (rest routes#)))))))
