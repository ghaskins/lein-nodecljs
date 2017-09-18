(ns lein-nodecljs.util
  (:require [clojure.java.io :as io]
            [leiningen.core.main]))

(def outputpath "src")

(defn get-config [{{:keys [path]} :nodecljs :keys [target-path] :as project}]
  (let [workdir (if path
                  (io/file path)
                  (io/file target-path "nodecljs"))
        outputdir (io/file workdir outputpath)
        mainjs (io/file workdir "main.js")]

    {:workdir workdir :outputdir outputdir :mainjs mainjs}))

(defn get-deps [project]
  (leiningen.core.main/apply-task "nodedeps" project []))

(defn run-compiler [project]
  (leiningen.core.main/apply-task "nodecompile" project []))
