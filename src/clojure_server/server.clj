(ns clojure-server.server)

(defn open-server-socket [port address]
  (java.net.ServerSocket. port 0 (java.net.InetAddress/getByName address)))
