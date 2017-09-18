(ns lein-nodecljs.package
  (:require [clojure.java.io :as io]
            [cheshire.core :as json]))

(defn emit-json [{{bin :bin} :nodecljs :keys [name description version url npm]} workdir]
  (let [path (io/file workdir "package.json")
        json {:name name
              :description description
              :version version
              :homepage url
              :dependencies (->> npm
                                 :dependencies
                                 (into (sorted-map)))
              :bin {(or bin name) "./main.js"}}
        content (json/generate-string json {:pretty true})]

    ;; ensure the path exists
    (io/make-parents path)

    ;; and blast it out to the filesystem
    (spit path content :truncate true)))
