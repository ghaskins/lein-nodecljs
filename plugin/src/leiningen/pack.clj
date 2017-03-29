(ns leiningen.pack
  (:require [lein-nodecljs.util :as util]
            [lein-nodecljs.exec :refer :all]))

(defn pack
  "Packages a nodecljs project with 'npm pack', suitable for installation or deployment to npmjs.org"
  [project & args]

  ;; Run the compiler first
  (util/run-compiler project)

  ;; Then package everything up with npm-pack
  (let [{:keys [workdir]} (util/get-config project)]
    (println "[npm] Packaging source")
    (npm "pack" "--verbose" :dir (.getCanonicalPath workdir))))
