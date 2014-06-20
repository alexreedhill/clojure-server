(ns lazy-server.router
  (:require [lazy-server.response-builder :refer :all]
            [lazy-server.basic-authenticator :refer [basic-auth]]
            [lazy-server.file-interactor :refer [file-exists? read-file]]
            [digest :refer [sha1]]))

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

(defn if-match-header-matches? [request file-contents]
  (= (sha1 file-contents) ((request :headers) "If-Match")))

(defmacro gen-patch-response-fn [request-sym response]
  `(fn [~request-sym]
     (let [file-contents# (read-file (str "public/" (~request-sym :path)))]
       (if (if-match-header-matches? ~request-sym file-contents#)
         (build ~request-sym {:code 204 :headers {"Etag" (sha1 (~request-sym :body))}})
         (build ~request-sym {:code 412 :headers {"Etag" (sha1 file-contents#)}})))))

(defmacro PATCH [path response request-sym]
  `(let [response-fn# (gen-patch-response-fn ~request-sym ~response)]
     (generate-handler ~path ~request-sym response-fn# "PATCH")))

(defmacro OPTIONS [path response request-sym]
  `(let [response-fn# (fn [~request-sym] (build ~request-sym (options-response ~response)))]
     (generate-handler ~path ~request-sym response-fn# "OPTIONS")))

(defn add-method-if-allowed [allowed routes request]
  (cond
    (path-matches? request (second (first routes)))
    (conj allowed (str (first (first routes))))
    (and (= (count routes) 1) (file-exists? (str "public/" (request :path))))
    (conj allowed "GET")
    :else allowed))

(defmacro get-allowed-methods [request request-sym routes]
  `(loop [routes# '~routes
          allowed#     []]
     (if (= (count routes#) 0)
         (sort allowed#)
         (recur (rest routes#) (add-method-if-allowed allowed# routes# ~request)))))

(defmacro client-error [request request-sym routes]
  `(let [allowed# (get-allowed-methods ~request ~request-sym ~routes)]
     (if (> (count allowed#) 0)
       (build ~request (method-not-allowed-response allowed#))
       (build ~request {:code 404 :body (last (last '~routes))}))))

(defmacro not-found [request request-sym routes]
  `(let [file-path# (str "public/" (~request :path))]
     (if (and (file-exists? file-path#) (= (~request :method) "GET"))
       ((GET (~request :path) (serve-file ~request) ~request-sym) ~request)
       (client-error ~request ~request-sym ~routes))))

(defn not-found? [routes]
  (or (= (count routes) 0)
    (and (= (count routes) 1) (= (first (last routes)) 'not-found))))

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
