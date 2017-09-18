(ns leiningen.nodecompile
  (:require [lein-nodecljs.package :as package]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [leiningen.core.main :as lein.main]
            [leiningen.core.eval :as eval]
            [lein-nodecljs.exec :refer :all]
            [lein-nodecljs.util :as util]
            [me.raynes.fs :refer :all])
  (:refer-clojure :exclude [compile]))

(defn nodecompile
  "Compiles a nodecljs project's ClojureScript code into a nodejs module"
  [{{main :main files :files} :nodecljs :keys [source-paths] :as project}]

  (let [{:keys [workdir outputdir mainjs]} (util/get-config project)
        opts {:main (str main)
              :output-to (.getCanonicalPath mainjs)
              :output-dir (.getCanonicalPath outputdir)
              :asset-path util/outputpath
              :source-map true
              :optimizations :none
              :target :nodejs
              :pretty-print true}]

    (package/emit-json project workdir)

    ;; Copy resources
    (when-let [inputs (->> files (map io/file) (map file-seq) flatten (filter file?))]
      (lein.main/info "[nodecljs] Copying resources")
      (doseq [input inputs]
        (let [ipath (.getPath input)
              output (io/file workdir ipath)]
          (io/make-parents output)
          (io/copy input output))))

    ;; Run the compiler within project-context
    (lein.main/info "[nodecljs] Compiling")
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
