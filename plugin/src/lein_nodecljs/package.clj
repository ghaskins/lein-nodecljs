(ns lein-nodecljs.package
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [cheshire.core :as json]))

;;------------------------------------------------------------------------------
;; translate-version: this function takes a "version" string as input (from the
;; second item in an entry from the :npm { :dependencies []} array) and
;; determines if it happens to be a 'LocalPath' spec as defined:
;;
;;        https://docs.npmjs.com/files/package.json#local-paths
;;
;; If it is, it translates what is likely a relative path into an FQP such that
;; it becomes location independent.  This is helpful since we typically
;; synthesize the package.json in a path that differs from the location of
;; the project.clj which defined it.
;;
;; N.B. file: and translated paths are not advised for use in an official
;; release of your project if it will be pushed to NPM.  However, they are
;; handy during development
;;------------------------------------------------------------------------------
(defn- translate-version [version]
  (when-let [[_ path] (re-matches #"file:(.*)" version)]
    (let [file (io/file path)]
      (if (fs/exists? file)
        (str "file:" (.getCanonicalPath file))
        (throw (AssertionError. (str "npm: bad dependency " version)))))))

;;------------------------------------------------------------------------------
;; fixup-paths: run through all dependencies and translate any file: based
;; entries
;;------------------------------------------------------------------------------
(defn- fixup-paths [deps]
  (for [[name version] deps]
    [name (or (translate-version version) version)]))

;;------------------------------------------------------------------------------
;; emit-json: synthesizes a "package.json" file from our project settings
;;------------------------------------------------------------------------------
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
