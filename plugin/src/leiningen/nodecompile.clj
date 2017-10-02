(ns leiningen.nodecompile
  (:require [lein-nodecljs.package :as package]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [leiningen.core.main :as lein.main]
            [leiningen.core.eval :as eval]
            [lein-nodecljs.exec :refer :all]
            [lein-nodecljs.util :as util]
            [me.raynes.fs :refer :all])
  (:refer-clojure :exclude [compile]))

(def cli-options
  [["-d" "--debug" "generate a debug build"]
   [nil "--disable-parallel" "run the compiler without parallel execution"]
   ["-p" "--profile PROFILE" "select the cljsbuild profile"
    :default "nodecljs"]])

(defn prep-usage [msg] (->> msg flatten (string/join \newline)))

(defn usage [options-summary]
  (prep-usage ["Usage: lein nodecompile [options]"
               ""
               "Options Summary:"
               options-summary
               ""]))

(defn normalize-profiles [profiles]
  (cond

    (map? profiles)
    profiles

    :else
    (->> profiles (map (fn [e] [(:id e) e])) (into {}))))


(defn nodecompile
  "Compiles a nodecljs project's ClojureScript code into a nodejs module"
  [{{main :main files :files} :nodecljs :keys [source-paths] :as project} & args]

  (let [{{:keys [debug disable-parallel profile]} :options :keys [summary errors]} (parse-opts args cli-options)
        {:keys [workdir outputdir mainjs]} (util/get-config project)
        build-profile (-> project
                          :cljsbuild
                          :builds
                          normalize-profiles
                          (get profile))
        opts (-> (:compiler build-profile)
                 (cond-> (not disable-parallel)
                   (assoc :parallel-build true))
                 (cond-> (not debug)
                   (assoc :static-fns true
                          :fn-invoke-direct true
                          :optimize-constants true)))]

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
    (lein.main/info (str "[nodecljs] Compiling using " opts))
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
