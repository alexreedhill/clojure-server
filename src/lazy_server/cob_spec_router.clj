(ns lazy-server.cob-spec-router
  (:require [lazy-server.router :refer :all]))

(defrouter cob-spec-router
  (GET "/" {:body "" :code 200}))
