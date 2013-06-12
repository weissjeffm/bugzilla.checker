(defproject bugzilla.checker "0.2.0-SNAPSHOT"
  :description "A lightweight bugzilla client that checks for whether bugs are still open."
  :jvm-opts ["-Xmx128m"]
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [clj-http "0.3.3"]
                 [slingshot "0.10.2"]
                 [org.clojure/data.json "0.1.2"]])
