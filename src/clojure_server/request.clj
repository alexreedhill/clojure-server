(ns clojure-server.request)
(use '[clojure.string :only (split)])

(defn parse-params [params outer-delimiter inner-delimiter]
  (loop [acc          {}
         split-params (split params outer-delimiter)]
    (if (= (count split-params) 0)
      acc
      (let [split-param (split (first split-params) inner-delimiter)]
        (recur (merge acc {(first split-param) (second split-param)})
               (rest split-params))))))

(defn parse-status-line [raw-status-line]
  (let [split-status-line (split raw-status-line #" ")]
    {:method (split-status-line 0)
     :path (split-status-line 1)
     :params (parse-params (nth (split (split-status-line 1) #"\?") 1 "") #"&" #"=")
     :http-version (split-status-line 2)}))

(defn parse [raw-request]
  (let [split-request (split raw-request #"\r\n")]
      (merge
        (parse-status-line (nth split-request 0))
        {:headers (parse-params (nth split-request 1 "") #"\n" #": ")}
        {:body (nth split-request 2 "")})))
