(ns lein-nodecljs.core
  (:require [clojure.java.io :as io]
            [cheshire.core :as json]
            [clojure.string :as string]
            [cljs.build.api :as build])
  (:refer-clojure :exclude [compile]))

(defn- emit-packagejson [{{bin :bin} :nodecljs :keys [name description version url npm]} workdir]
  (let [path (io/file workdir "package.json")
        content (json/generate-string {:name name
                                       :description description
                                       :version version
                                       :homepage url
                                       :dependencies (->> npm
                                                          :dependencies
                                                          (into (sorted-map)))
                                       :bin {(or bin name) "./main.js"}}
                                      {:pretty true})]
    ;; ensure the path exists
    (io/make-parents path)

    ;; and blast it out to the filesystem
    (spit path content :truncate true)))

(defn compile [{{main :main} :nodecljs :keys [source-paths target-path] :as project}]

  (let [target (io/file target-path)
        workdir (io/file target "nodecljs")
        outputdir (io/file workdir "src")
        mainjs (io/file workdir "main.js")]

    (println "[cljs] Compiling")

    ;; Emit the package.json file
    (emit-packagejson project workdir)

    ;; Emit the javascript code
    (build/build
     (apply build/inputs source-paths)
     {:main main
      :output-to (.getCanonicalPath mainjs)
      :output-dir (.getCanonicalPath outputdir)
      :asset-path "src"
      :source-map true
      :optimizations :none
      :target :nodejs
      :pretty-print true})

    ;; Fix up the emitted code to address CLJS-1990 (http://dev.clojure.org/jira/browse/CLJS-1990)
    (let [patch (-> (slurp mainjs)
                    (string/replace "path.resolve(\".\")", "__dirname"))]
      (spit mainjs patch))

    ;; Finally, return our working dir
    workdir))
