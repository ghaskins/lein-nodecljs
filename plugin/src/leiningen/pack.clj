(ns leiningen.pack
  (:require [nodecljs.core :as core]
            [clojure.java.shell :refer [sh]]))

(defn pack
  "Packages a nodecljs project with 'npm pack', suitable for installation or deployment to npmjs.org"
  [project & args]

  (let [workdir (core/compile project)]
    ;; And then package it up
    (sh "npm" "pack" (.getCanonicalPath workdir))))
