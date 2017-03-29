(ns lein-nodecljs.exec
  (:require [leiningen.core.main :as lein.main]
            [clojure.java.shell :refer [sh]]))

(defn- exec [args]
  (let [{:keys [err out exit]} (apply sh args)]

    (when (not (zero? exit))
      (lein.main/warn err))

    (lein.main/debug out)

    exit))

(defn npm [& args]
  (exec (cons "npm" args)))

(defn node [& args]
  (exec (cons "node" args)))
