(ns lein-nodecljs.plugin
  (:require [lein-nodecljs.core :as core]
            [robert.hooke]
            [leiningen.install]
            [leiningen.compile]
            [clojure.java.shell :refer [sh]]))

(defn- install-hook
  [f project & args]

  (apply f project args)

  (let [workdir (core/compile project)]

    (println "[npm] Installing")
    (let [retval (sh "npm" "install" "-g" (.getCanonicalPath workdir))]

      (println (:err retval))

      (when (-> retval :exit zero?)
        (println (:out retval))))))

(defn- compile-hook
  [f project & args]

  (apply f project args)

  (let [workdir (core/compile project)]

    (println "[npm] Compiling")
    (let [retval (sh "npm" "install" :dir (.getCanonicalPath workdir))]

      (println (:err retval))

      (when (-> retval :exit zero?)
        (println (:out retval))))))

(defn hooks []
  (robert.hooke/add-hook #'leiningen.install/install install-hook)
  (robert.hooke/add-hook #'leiningen.compile/compile compile-hook))
