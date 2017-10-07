(ns lein-nodecljs.plugin
  (:require [leiningen.core.main :as lein.main]
            [leiningen.core.project :as lein.project]
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

  (util/get-deps project)
  (util/run-compiler project)

  (let [{:keys [workdir]} (util/get-config project)]

    (lein.main/info "[node] Launching")
    (node "main.js" :dir (.getCanonicalPath workdir))))

(defn hooks []
  (robert.hooke/add-hook #'leiningen.install/install install-hook)
  (robert.hooke/add-hook #'leiningen.run/run run-hook))

(def default-cljsbuild ['lein-cljsbuild "1.1.7" :exclusions [['org.clojure/clojure]]])
(def default-figwheel ['lein-figwheel "0.5.13"])
(def default-sourcemap ['source-map-support "0.4.15"])

(defn middleware [{{main :main files :files} :nodecljs :keys [source-paths] :as project}]
  (if-not (-> project :nodecljs :autoprofile)

    (let [{:keys [workdir outputdir mainjs]} (util/get-config project)
          plugins (->> project :plugins (map first) (into #{}))
          deps (->> project :npm :dependencies (map first) (into #{}))
          builds (->> project :cljsbuild :builds)
          nodecljs-build {
                          :id "nodecljs"
                          :source-paths source-paths
                          :figwheel true
                          :compiler {
                                     :main (str main)
                                     :asset-path util/outputpath
                                     :output-to (.getCanonicalPath mainjs)
                                     :output-dir (.getCanonicalPath outputdir)
                                     :target :nodejs
                                     :optimizations :none
                                     :pretty-print true
                                     :source-map true}}

          autoprofile (-> {:nodecljs {:autoprofile true}}

                          ;; auto-insert lein-cljsbuild if not present
                          (cond-> (not ('lein-cljsbuild plugins))
                            (update-in [:plugins] conj default-cljsbuild))

                          ;; auto-insert lein-figwheel if not present
                          (cond-> (not ('lein-figwheel plugins))
                            (update-in [:plugins] conj default-figwheel))

                          ;; auto-insert source-map-support if not present
                          (cond-> (not ('source-map-support deps))
                            (update-in [:npm :dependencies] conj default-sourcemap))

                          ;; auto-insert our nodecljs build profile
                          (cond-> (or (nil? builds) (vector? builds))
                            (update-in [:cljsbuild :builds] conj nodecljs-build))
                          (cond-> (map? builds)
                            (update-in [:cljsbuild :builds] assoc :nodecljs nodecljs-build)))]
      (lein.project/merge-profiles project [autoprofile]))

    ;; else, just pass the project through unscathed
    project))
