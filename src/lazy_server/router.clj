(ns lazy-server.router
  (:require [lazy-server.response-builder :refer [build]]
            [lazy-server.file-interactor :refer [file-exists? read-file write-to-file
                                                 read-partial-file]]
            [lazy-server.basic-authenticator :refer [authenticate]]
            [clojure.string :refer [join split]]
            [pantomime.mime :refer [mime-type-of]]
            [digest :refer [sha1]]))

(def public-dir "public/")

(defn path-matches? [request path]
  (= path (request :path)))

(defn method-matches? [request method]
  (= method (request :method)))

(defn request-matches? [request path method]
  (and (method-matches? request method) (path-matches? request path)))

(defn redirect [path]
  {:code 301 :headers {"Location" path}})

(defn save-resource [request]
  (if-let [file-saved (write-to-file (str public-dir (request :path)) (request :body))]
    {:code 200}
    {:code 500}))

(defn file-response [file-contents request success-code]
  (if (nil? file-contents)
    {:code 404}
    {:code success-code
     :headers {"Content-Type" (mime-type-of (request :path))}
     :body file-contents}))

(defn serve-partial-file [request]
  (let [range (-> ((request :headers) "Range")
                   (split #"=")
                   second)
        [min max] (map read-string (split range #"-"))
        file-contents (read-partial-file (str public-dir (request :path)) min max)]
    (file-response file-contents request 206)))

(defn serve-entire-file [request]
  (let [file-contents (read-file (str public-dir (request :path)))]
    (file-response file-contents request 200)))

(defn serve-file [request]
  (if (and (request :headers) ((request :headers) "Range"))
    (serve-partial-file request)
    (serve-entire-file request)))

(defn resolve-response [request handler]
  (if (= (type handler) clojure.lang.PersistentList)
    (let [handler-ns (get :ns (meta (resolve (first handler))) 'lazy-server.router)
          handler (replace {'request request} handler)]
      (apply (ns-resolve handler-ns (first handler)) (rest handler)))
    handler))

(defn GET [request handler]
  (build (resolve-response request handler)))

(defn POST [request handler]
  (build (resolve-response request handler)))

(defn PUT [request handler]
  (build (resolve-response request handler)))

(defn options-response [response]
  (assoc response :headers {"Allow" "GET,HEAD,POST,OPTIONS,PUT"}))

(defn OPTIONS [request handler]
  (build (options-response (resolve-response request handler))))

(defn if-match-header-matches? [request file-contents]
  (= (sha1 file-contents) ((request :headers) "If-Match")))

(defn PATCH [request handler]
  (let [file-contents (read-file (str public-dir (request :path)))]
    (if (if-match-header-matches? request file-contents)
      (do
        (:body (resolve-response request handler))
        (build {:code 204 :headers {"Etag" (sha1 (request :body))}}))
      (build {:code 412 :headers {"Etag" (sha1 file-contents)}}))))

(defn method-not-allowed-response [allowed]
  {:code 405 :headers {"Allow" (join "," allowed)}})

(defn get-allowed-methods [request routes]
  (->> routes
       (filter #(= (request :path) (second %)))
       (map #(first %))
       (#(if (file-exists? (str public-dir (request :path))) (conj % "GET") %))))

(defn client-error [request routes]
  (let [allowed (get-allowed-methods request routes)
        response-body (or (last (last routes)) "Sorry, there's nothing here!")]
     (if (> (count allowed) 0)
       (build (method-not-allowed-response allowed))
       (build {:code 404 :body response-body}))))

(defn not-found [request routes]
  (let [file-path (str public-dir (request :path))]
     (if (and (file-exists? file-path) (= (request :method) "GET"))
       (GET request (serve-file request))
       (client-error request routes))))

(defn resolve-method-fn [request]
  (ns-resolve 'lazy-server.router (symbol (request :method))))

(defmacro route-functionizer [request & route]
  `(fn [request#]
     (let [method-fn# (resolve-method-fn request#)
           handler# '~(last route)]
       (method-fn# request# handler#))))

(defmacro route-validator [route request-sym]
  `(fn [request#]
     (let [route-method# (str '~(first route))
           route-path# '~(second route)
           route-fn#    (route-functionizer request# ~@route)]
       (if (request-matches? request# route-path# route-method#)
         (route-fn# request#)))))

(defmacro routes-to-fns
  ([request-sym route]
   `[(route-validator ~route ~request-sym)])
  ([request-sym route & more-routes]
   `(concat
      [(route-validator ~route ~request-sym)]
      (routes-to-fns ~request-sym ~@more-routes))))

(defmacro routes-to-router-fn [request-sym & routes]
  `(fn [request#]
     (let [fns# (routes-to-fns ~request-sym ~@routes)
           success-fn# (apply some-fn fns#)]
       (or
         (success-fn# request#)
         (not-found request# '~routes)))))

(defmacro defrouter [router-name request-sym & routes]
  `(defn ~router-name [request#]
     (let [router-fn# (routes-to-router-fn ~request-sym ~@routes)]
       (router-fn# request#))))
