(ns nodecljs.core
  (:require [clojure.java.io :as io]
            [clojure.tools.file-utils :as fileutils]
            [cheshire.core :as json]
            [cljs.build.api :as build])
  (:refer-clojure :exclude [compile]))

(defn- emit-packagejson [{:keys [name description version url npm]} workdir]
  (let [path (io/file workdir "package.json")
        content (json/generate-string {:name name
                                       :description description
                                       :version version
                                       :homepage url
                                       :dependencies (->> npm
                                                          :dependencies
                                                          (into (sorted-map)))
                                       :bin {name "./main.js"}}
                                      {:pretty true})]
    ;; ensure the path exists
    (io/make-parents path)

    ;; and blast it out to the filesystem
    (spit path content :truncate true)))

(defn compile [{:keys [target-path main] :as project}]

  (let [target (io/file target-path)
        workdir (io/file target "nodecljs")
        outputdir (io/file workdir "src")
        mainjs (io/file workdir "main.js")]

    (println "[cljs] Compiling")

    ;; Blow away our working dir to ensure we build fresh
    (fileutils/recursive-delete workdir)

    ;; Emit the package.json file
    (emit-packagejson project workdir)

    ;; Emit the javascript code
    (build/build "src" {:main main
                        :output-to (.getCanonicalPath mainjs)
                        :output-dir (.getCanonicalPath outputdir)
                        :asset-path "src"
                        :source-map true
                        :optimizations :none
                        :target :nodejs
                        :pretty-print true})

    ;; Finally, return our working dir
    workdir))
