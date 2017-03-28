(ns lein-nodecljs.npm
  (:require [clojure.java.shell :refer [sh]]))

(defn npm [& args]
  (let [{:keys [err out exit]} (apply sh (cons "npm" args))]

    (println err)

    (when (zero? exit)
      (println out))

    exit))
