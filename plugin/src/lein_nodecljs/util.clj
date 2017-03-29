(ns lein-nodecljs.util
  (:require [clojure.java.io :as io]
            [leiningen.core.main]))

(def outputpath "src")

(defn get-config [{:keys [target-path] :as project}]
    (let [target (io/file target-path)
          workdir (io/file target "nodecljs")
          outputdir (io/file workdir outputpath)
          mainjs (io/file workdir "main.js")]

      {:workdir workdir :outputdir outputdir :mainjs mainjs}))

(defn run-compiler [project]
  (leiningen.core.main/apply-task "nodecompile" project []))
