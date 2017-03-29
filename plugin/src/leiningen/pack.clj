(ns leiningen.pack
  (:require [leiningen.core.main :as lein.main]
            [lein-nodecljs.util :as util]
            [lein-nodecljs.exec :refer :all]))

(defn pack
  "Packages a nodecljs project with 'npm pack', suitable for installation or deployment to npmjs.org"
  [project & args]

  ;; Run the compiler first
  (util/run-compiler project)

  ;; Then package everything up with npm-pack
  (let [{:keys [workdir]} (util/get-config project)]
    (lein.main/info "[npm] Packaging source")
    (npm "pack" "--verbose" :dir (.getCanonicalPath workdir))))
