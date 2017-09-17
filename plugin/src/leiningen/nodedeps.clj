(ns leiningen.nodedeps
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [cheshire.core :as json]
            [clojure.tools.cli :refer [parse-opts]]
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

(def cli-options
  [["-g" "--global" "install npm dependencies globally"]])

(defn prep-usage [msg] (->> msg flatten (string/join \newline)))

(defn usage [options-summary]
  (prep-usage ["Usage: lein nodedeps [options]"
               ""
               "Options Summary:"
               options-summary
               ""]))

(defn nodedeps
  "Downloads a ClojureScript's projects nodejs dependencies"
  [project & args]

  (let [{:keys [options summary errors]} (parse-opts args cli-options)
        {:keys [workdir]} (util/get-config project)]

    (cond

      (not= errors nil)
      (do
        (lein.main/warn (str "Error: " (string/join errors)))
        (lein.main/warn (usage summary))
        (lein.main/exit -1))

      :else
      (do
        ;; Emit the package.json file
        (emit-packagejson project workdir)

        (if (:global options)
          (do
            (lein.main/info "[npm] Installing global dependencies")
            (npm "install" "-g" :dir (.getCanonicalPath workdir))

            ;; There is a well known issue where NPM sometimes fails to properly
            ;; install dependencies if you only run it once
            (lein.main/debug "[npm] Running second phase install")
            (npm "install" "-g" :dir (.getCanonicalPath workdir)))
          (do
            (lein.main/info "[npm] Installing local dependencies")
            (npm "install" :dir (.getCanonicalPath workdir))))))))
