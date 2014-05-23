(ns lazy-server.router
  (:require [clojure.string :refer [upper-case]]))

(defmacro defrouter [router-name request & routes]
  `(defn ~router-name [request#]
     (loop [[current-route# & rest-routes# :as routes#] '~routes]
       (cond
         (= 0 (count routes#)) [nil 404]
         (and
           (= (second current-route#) (request# :path))
           (= (upper-case (first current-route#)) (request# :method)))
           (last current-route#)
         :else (recur rest-routes#)))))
