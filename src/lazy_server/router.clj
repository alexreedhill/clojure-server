(ns lazy-server.router
  (:require [lazy-server.response-builder :refer [build]]))

(defmacro GET [path response request-sym]
  `(let [handler# (fn [~request-sym] (build ~request-sym ~response))]
     (fn [request#]
       (if (and (= ~path (request# :path)) (= (request# :method) "GET"))
         (handler# request#)))))

(defmacro four-oh-four [body request-sym]
  `(let [handler# (fn [~request-sym] (build ~request-sym {:code 404 :body ~body}))]
     (fn [request#] (handler# request#))))

(defmacro defrouter [router-name request-sym & routes]
  `(defn ~router-name [request#]
     (loop [routes# '~routes]
       (let [response# ((eval (concat (first routes#) '(~request-sym))) request#)]
         (if (not (nil? response#))
           response#
           (recur (rest routes#)))))))
