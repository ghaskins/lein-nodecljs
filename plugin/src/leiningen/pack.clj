(ns leiningen.pack
  (:require [lein-nodecljs.core :as core]
            [lein-nodecljs.npm :refer :all]
            [clojure.java.shell :refer [sh]]))

(defn pack
  "Packages a nodecljs project with 'npm pack', suitable for installation or deployment to npmjs.org"
  [project & args]

  (let [workdir (core/compile project)]

    (println "[npm] Packaging source")
    (npm "pack" "--verbose" :dir (.getCanonicalPath workdir))))
