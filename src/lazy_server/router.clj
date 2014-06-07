(ns lazy-server.router
  (:require [lazy-server.response-builder :refer [build options-response]]))

(defn request-matches? [request path method]
  (and (= method (request :method)) (= path (request :path))))

(defmacro generate-handler [path response request-sym handler-fn method]
  `(fn [request#]
     (if (request-matches? request# ~path ~method)
       (~handler-fn request#))))

(defmacro GET [path response request-sym]
  `(let [handler-fn# (fn [~request-sym] (build ~request-sym ~response))]
     (generate-handler ~path ~response ~request-sym handler-fn# "GET")))

(defmacro POST [path response request-sym]
  `(let [handler-fn# (fn [~request-sym] (build ~request-sym ~response))]
     (generate-handler ~path ~response ~request-sym handler-fn# "POST")))

(defmacro PUT [path response request-sym]
  `(let [handler-fn# (fn [~request-sym] (build ~request-sym ~response))]
     (generate-handler ~path ~response ~request-sym handler-fn# "PUT")))

(defmacro OPTIONS [path response request-sym]
  `(let [handler-fn# (fn [~request-sym] (build ~request-sym (options-response ~response)))]
     (generate-handler ~path ~response ~request-sym handler-fn# "OPTIONS")))

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
