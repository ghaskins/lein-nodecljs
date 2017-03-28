(ns lein-nodecljs.exec
  (:require [clojure.java.shell :refer [sh]]))

(defn- exec [args]
  (let [{:keys [err out exit]} (apply sh args)]

    (println err)

    (when (zero? exit)
      (println out))

    exit))

(defn npm [& args]
  (exec (cons "npm" args)))

(defn node [& args]
  (exec (cons "node" args)))
