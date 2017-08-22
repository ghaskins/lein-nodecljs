(ns leiningen.nodedeps
  (:require [clojure.java.io :as io]
            [cheshire.core :as json]
            [leiningen.core.main :as lein.main]
            [lein-nodecljs.exec :refer :all]
            [lein-nodecljs.util :as util])
  (:refer-clojure :exclude [compile]))

(defn- emit-packagejson [{{bin :bin} :nodecljs :keys [name description version url npm]} workdir]
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

(defn nodedeps
  "Downloads a ClojureScript's projects nodejs dependencies"
  [project]

  (let [{:keys [workdir]} (util/get-config project)]

    ;; Emit the package.json file
    (emit-packagejson project workdir)

    (lein.main/info "[npm] Installing Dependencies")
    (npm "install" :dir (.getCanonicalPath workdir))))
