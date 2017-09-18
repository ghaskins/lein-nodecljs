(ns leiningen.nodedeps
  (:require [lein-nodecljs.package :as package]
            [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [leiningen.core.main :as lein.main]
            [lein-nodecljs.exec :refer :all]
            [lein-nodecljs.util :as util])
  (:refer-clojure :exclude [compile]))

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
        (package/emit-json project workdir)

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
