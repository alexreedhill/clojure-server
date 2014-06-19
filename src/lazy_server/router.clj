(ns lazy-server.router
  (:require [lazy-server.response-builder :refer :all]
            [lazy-server.basic-authenticator :refer [basic-auth]]
            [lazy-server.file-interactor :refer [file-exists?]]))

(defn path-matches? [request path]
  (= path (request :path)))

(defn method-matches? [request method]
  (= method (request :method)))

(defn request-matches? [request path method]
  (and (method-matches? request method) (path-matches? request path)))

(defmacro generate-handler [path request-sym response-fn method]
  `(fn [request#]
     (if (request-matches? request# ~path ~method)
       (~response-fn request#))))

(defmacro GET [path response request-sym]
  `(let [response-fn# (fn [~request-sym] (build ~request-sym ~response))]
     (generate-handler ~path ~request-sym response-fn# "GET")))

(defmacro POST [path response request-sym]
  `(let [response-fn# (fn [~request-sym] (build ~request-sym ~response))]
     (generate-handler ~path ~request-sym response-fn# "POST")))

(defmacro PUT [path response request-sym]
  `(let [response-fn# (fn [~request-sym] (build ~request-sym ~response))]
     (generate-handler ~path ~request-sym response-fn# "PUT")))

(defmacro OPTIONS [path response request-sym]
  `(let [response-fn# (fn [~request-sym] (build ~request-sym (options-response ~response)))]
     (generate-handler ~path ~request-sym response-fn# "OPTIONS")))

(defn four-oh-four? [routes]
  (= (count routes) 0))

(defn allowed-methods-found? [routes allowed]
  (and (= (count routes) 0) (> (count allowed) 0)))

(defmacro client-error [request request-sym routes]
  `(loop [routes# '~routes
         allowed#     []]
    (cond
      (allowed-methods-found? routes# allowed#)
      (build ~request (method-not-allowed-response allowed#))
      (four-oh-four? routes#)
      (build ~request {:code 404 :body (last (last '~routes))})
      (path-matches? ~request (second (first routes#)))
      (recur (rest routes#) (conj allowed# (first (first routes#))))
      :else (recur (rest routes#) allowed#))))

(defmacro not-found [request request-sym routes]
  `(let [file-path# (str "public/" (~request :path))]
     (if (file-exists? file-path#)
       ((GET (~request :path) (serve-file ~request) ~request-sym) ~request)
       (client-error ~request ~request-sym ~routes))))

(defn not-found? [routes]
  (and (= (count routes) 1) (= (first (last routes)) 'not-found)))

(defn local-eval [body]
  (binding [*ns* (find-ns 'lazy-server.router)]
    (eval body)))

(defmacro defrouter [router-name request-sym & routes]
  `(defn ~router-name [request#]
     (loop [routes# '~routes]
       (if (not-found? routes#)
         (not-found request# ~request-sym ~routes)
         (if-let [response# ((local-eval (concat (first routes#) '(~request-sym))) request#)]
           response#
           (recur (rest routes#)))))))
