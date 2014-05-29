(ns lazy-server.router)

(defn status-line-matches? [route request]
  (and
    (= (str (first route)) (request :method))
    (= (second route) (request :path))))

(defmacro defrouter [router-name & routes]
  `(defn ~router-name [request#]
     (loop [[current-route# & rest-routes# :as routes#] '~routes]
       (cond
         (= 0 (count routes#)) {:body nil :code 404}
         (status-line-matches? current-route# request#) (last current-route#)
         :else (recur rest-routes#)))))
