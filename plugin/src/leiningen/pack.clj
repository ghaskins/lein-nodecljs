(ns leiningen.pack
  (:require [lein-nodecljs.core :as core]
            [clojure.java.shell :refer [sh]]))

(defn pack
  "Packages a nodecljs project with 'npm pack', suitable for installation or deployment to npmjs.org"
  [project & args]

  (let [workdir (core/compile project)]

    (println "[npm] Packaging source")
    (let [retval (sh "npm" "pack" "--verbose" (.getCanonicalPath workdir))]

      (println (:err retval))

      (when (-> retval :exit zero?)
        (println "Wrote" (:out retval))))))
