(ns lazy-server.parameter-decoder-spec
  (:require [lazy-server.parameter-decoder :refer :all]
            [speclj.core :refer :all]))


(describe "parameter decoder"
  (it "decodes characters required by cob-spec"
    (should=
        "string=Operators <, >, =, !=; +, -, *, &, @, #, $, [, ]: \"is that all\"?"
        (decode (str "string=Operators%20%3C%2C%20%3E%2C%20%3D%2C%20!%3D%3B"
                      "%20%2B%2C%20-%2C%20*%2C%20%26%2C%20%40%2C%20%23%2C%2"
                      "0%24%2C%20%5B%2C%20%5D%3A%20%22is%20that%20all%22%3F")))))
