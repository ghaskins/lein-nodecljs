(ns leiningen.nodecompile
  (:require [clojure.java.io :as io]
            [cheshire.core :as json]
            [clojure.string :as string]
            [leiningen.core.main :as lein.main]
            [leiningen.core.eval :as eval]
            [lein-nodecljs.util :as util])
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

(defn nodecompile
  "Compiles a nodecljs project's ClojureScript code into a nodejs module"
  [{{main :main} :nodecljs :keys [source-paths target-path] :as project}]

  (let [{:keys [workdir outputdir mainjs]} (util/get-config project)
        opts {:main (str main)
              :output-to (.getCanonicalPath mainjs)
              :output-dir (.getCanonicalPath outputdir)
              :asset-path util/outputpath
              :source-map true
              :optimizations :none
              :target :nodejs
              :pretty-print true}]

    (lein.main/info "[nodecljs] Compiling")

    ;; Emit the package.json file
    (emit-packagejson project workdir)

    ;; Run the compiler within project-context
    (eval/eval-in-project project
                          `(let [inputs# (apply cljs.build.api/inputs [~@source-paths])]
                             ;; Emit the javascript code
                             (cljs.build.api/build inputs# ~opts))
                          '(require 'cljs.build.api))

    ;; Fix up the emitted code to address CLJS-1990 (http://dev.clojure.org/jira/browse/CLJS-1990)
    (let [patch (-> (slurp mainjs)
                    (string/replace "path.resolve(\".\")", "__dirname"))]
      (spit mainjs patch))

    ;; Finally, return our working dir
    workdir))
