(ns lazy-server.router
  (:require [lazy-server.response-builder :refer [build options-response method-not-allowed-response]]))

(defn path-matches? [request path]
  (= path (request :path)))

(defn method-matches? [request method]
  (= method (request :method)))

(defn request-matches? [request path method]
  (and (method-matches? request method) (path-matches? request path)))

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

(defn allowed-methods? [routes allowed]
  (and (= (count routes) 0) (> (count allowed) 0)))

(defn four-oh-four? [routes]
  (= (count routes) 0))

(defmacro client-error [request request-sym routes]
  `(loop [routes# '~routes
          allowed#     []]
     (cond
       (allowed-methods? routes# allowed#) (build ~request (method-not-allowed-response allowed#))
       (four-oh-four? routes#) (build ~request {:code 404 :body (last (last '~routes))})
       (path-matches? ~request (second (first routes#))) (recur (rest routes#) (conj allowed# (first (first routes#))))
       :else (recur (rest routes#) allowed#))))

(defn client-error? [routes]
  (and (= (count routes) 1) (= (first (last routes)) 'not-found)))

(defmacro defrouter [router-name request-sym & routes]
  `(defn ~router-name [request#]
     (loop [routes# '~routes]
       (let [route# (concat (first routes#) '(~request-sym))]
         (cond
           (client-error? routes#) (client-error request# ~request-sym ~routes)
           (not (nil? ((eval route#) request#))) ((eval route#) request#)
           :else (recur (rest routes#)))))))
