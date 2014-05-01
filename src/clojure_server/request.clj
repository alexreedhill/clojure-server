(ns clojure-server.request)
(use '[clojure.string :only (split)])

(defn parse-params [query-string]
  (loop [acc          {}
         split-params (split query-string #"&")]
    (if (= (count split-params) 0)
      acc
      (let [split-param (split (first split-params) #"=")]
        (recur (merge acc {(first split-param) (second split-param)})
               (rest split-params))))))

(defn parse-status-line [raw-status-line]
  (let [split-status-line (split raw-status-line #" ")]
    {:method (split-status-line 0)
     :path (split-status-line 1)
     :params (parse-params (nth (split (split-status-line 1) #"\?") 1 ""))
     :http-version (split-status-line 2)}))

(defn parse-headers [raw-headers]
  (loop [acc           {}
         split-headers (split raw-headers #"\n")]
    (if (= (count split-headers) 0)
      {:headers acc}
      (let [split-header (split (first split-headers) #": ")]
        (recur (merge acc {(first split-header) (second split-header)})
               (rest split-headers))))))

(defn parse [raw-request]
  (let [split-request (split raw-request #"\r\n")]
      (merge
        (parse-status-line (nth split-request 0))
        (parse-headers (nth split-request 1 ""))
        {:body (nth split-request 2 "")})))
