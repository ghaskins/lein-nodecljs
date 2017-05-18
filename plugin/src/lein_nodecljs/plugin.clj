(ns lein-nodecljs.plugin
  (:require [leiningen.core.main :as lein.main]
            [lein-nodecljs.util :as util]
            [lein-nodecljs.exec :refer :all]
            [robert.hooke]
            [leiningen.install]
            [leiningen.compile]
            [leiningen.run]))

(defn- install-hook
  "Overrides the 'install' task with an 'npm install -g' action"
  [f project & args]

  ;; Delegate to the install function already present
  (apply f project args)

  ;; and then, run our own installation
  (util/run-compiler project)

  (let [{:keys [workdir]} (util/get-config project)]
    (lein.main/info "[npm] Installing")
    (npm "install" "-g" (.getCanonicalPath workdir))))

(defn- run-hook
  "Overrides the 'run' task to execute our program on nodejs platform"
  [f project & args]

  (util/run-compiler project)

  (let [{:keys [workdir]} (util/get-config project)]

    (lein.main/info "[node] Launching")
    (node "main.js" :dir (.getCanonicalPath workdir))))

(defn hooks []
  (robert.hooke/add-hook #'leiningen.install/install install-hook)
  (robert.hooke/add-hook #'leiningen.run/run run-hook))
