(ns lein-nodecljs.plugin
  (:require [lein-nodecljs.core :as core]
            [lein-nodecljs.exec :refer :all]
            [robert.hooke]
            [leiningen.install]
            [leiningen.compile]
            [leiningen.run]))

(defn- install-hook
  "Overrides the 'install' task with an 'npm install -g' action"
  [f project & args]

  (let [workdir (core/compile project)]

    (println "[npm] Installing")
    (npm "install" "-g" (.getCanonicalPath workdir))))

(defn- compile-hook
  "Overrides the 'compile' task to compile our nodejs application locally"
  [f project & args]

  (let [workdir (core/compile project)]

    (println "[npm] Compiling")
    (npm "install" :dir (.getCanonicalPath workdir))))

(defn- run-hook
  "Overrides the 'run' task to execute our program on nodejs platform"
  [f project & args]

  (let [workdir (core/compile project)]

    (println "[npm] Compiling")
    (npm "install" :dir (.getCanonicalPath workdir))

    (println "[node] Launching")
    (node "main.js" :dir (.getCanonicalPath workdir))))

(defn hooks []
  (robert.hooke/add-hook #'leiningen.install/install install-hook)
  (robert.hooke/add-hook #'leiningen.compile/compile compile-hook)
  (robert.hooke/add-hook #'leiningen.run/run run-hook))
