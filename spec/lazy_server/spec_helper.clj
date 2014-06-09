(ns lazy-server.spec-helper)

(defn bytes-to-string [bytes]
  (apply str (map char bytes)))
