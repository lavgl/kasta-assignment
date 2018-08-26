(defproject kasta-assignment "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.4.0"]
                 [compojure "1.5.1"]
                 [ring/ring-core "1.7.0-RC1"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-json "0.4.0"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [org.slf4j/log4j-over-slf4j "1.7.25"]
                 [prismatic/schema "1.1.9"]

                 [org.apache.kafka/kafka-clients "1.0.0"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler kasta-assignment.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
