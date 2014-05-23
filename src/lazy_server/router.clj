(ns lazy-server.router)

(defn status-line-matches? [route request]
  (and
    (= (route :path) (request :path))
    (= (route :method) (request :method))))

(defmacro defrouter [router-name request & routes]
  `(defn ~router-name [request#]
     (loop [[current-route# & rest-routes# :as routes#] '~routes]
       (cond
         (= 0 (count routes#)) {:body nil :code 404}
         (status-line-matches? current-route# request#) (current-route# :response)
         :else (recur rest-routes#)))))
