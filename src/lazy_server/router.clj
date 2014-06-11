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

(defmacro method-not-allowed [request request-sym routes]
  `(loop [routes# '~routes
          allowed#     []]
     (cond
       (and (= (count routes#) 0) (> (count allowed#) 0)) (build ~request (method-not-allowed-response allowed#))
       (= (count routes#) 0) ((eval (concat (last '~routes) '(~request-sym))) ~request)
       (path-matches? ~request (second (first routes#))) (recur (rest routes#) (conj allowed# (str (first (first routes#)))))
       :else (recur (rest routes#) allowed#))))

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

(defn last-route? [routes-left total-routes]
  (and (= (count routes-left) 1) (> (count total-routes) 1)))

(defmacro defrouter [router-name request-sym & routes]
  `(defn ~router-name [request#]
     (loop [routes# '~routes]
       (let [response# ((eval (concat (first routes#) '(~request-sym))) request#)]
         (cond
           (last-route? routes# '~routes) (method-not-allowed request# ~request-sym ~routes)
           (not (nil? response#)) response#
           :else (recur (rest routes#)))))))
