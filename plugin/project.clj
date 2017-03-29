(defproject lein-nodecljs "0.5.0"
  :description "A plugin for Leiningen to assist with building Clojurescript apps for NodeJS"
  :url "http://github.com/ghaskins/lein-nodecljs"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :lein-release {:deploy-via :clojars}
  :dependencies [[cheshire "5.7.0"]
                 [clojure-tools "1.1.3"]]
  :eval-in-leiningen true)
