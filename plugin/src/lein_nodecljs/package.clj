(ns lein-nodecljs.package
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [cheshire.core :as json]))

(defn- translate-version [version]
  (when-let [[_ path] (re-matches #"file:(.*)" version)]
    (let [file (io/file path)]
      (if (fs/exists? file)
        (str "file:" (.getCanonicalPath file))
        (throw (AssertionError. (str "npm: bad dependency " version)))))))

(defn- fixup-paths [deps]
  (for [[name version] deps]
    [name (or (translate-version version) version)]))

(defn emit-json [{{bin :bin} :nodecljs :keys [name description version url npm]} workdir]
  (let [path (io/file workdir "package.json")
        json {:name name
              :description description
              :version version
              :homepage url
              :dependencies (->> npm
                                 :dependencies
                                 fixup-paths
                                 (into (sorted-map)))
              :bin {(or bin name) "./main.js"}}
        content (json/generate-string json {:pretty true})]

    ;; ensure the path exists
    (io/make-parents path)

    ;; and blast it out to the filesystem
    (spit path content :truncate true)))
