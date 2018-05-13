(defproject lazy-server "0.2.0-SNAPSHOT"
  :description "Clojure HTTP server pet project"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:dev {:dependencies [[speclj "3.0.1"]]}}
  :plugins [[speclj "3.0.1"]]
  :test-paths ["spec"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.novemberain/pantomime "2.2.0"]
                 [org.clojure/data.codec "0.1.0"]
                 [digest "1.4.4"]]
  :main lazy-server.server)
