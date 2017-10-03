(defproject hello "1.0.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-nodecljs "0.11.0"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.495"]]
  :npm {:dependencies [[source-map-support "0.4.0"]]}
  :nodecljs {:main hello.core})
