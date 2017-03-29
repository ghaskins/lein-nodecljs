(defproject complex "1.0.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-nodecljs "0.5.0-SNAPSHOT"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.495"]
                 [funcool/promesa "1.3.1"]]
  :npm {:dependencies [[source-map-support "0.4.0"]
                       [protobufjs "5.0.1"]]}
  :nodecljs {:main complex.core})
