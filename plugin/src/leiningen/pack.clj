(ns leiningen.pack
  (:require [clojure.java.io :as io]
            [cheshire.core :as json]))

(defn- emit-packagejson [{:keys [name description version url npm]} workdir]
  (let [path (io/file workdir "package.json")
        content (json/generate-string {:name name
                                       :description description
                                       :version version
                                       :homepage url
                                       :dependencies (:dependencies npm)
                                       :bin {name "./main.js"}}
                                      {:pretty true})]
    ;; ensure the path exists
    (io/make-parents path)

    ;; and blast it out to the filesystem
    (spit path content :truncate true)))

(defn pack
  "Packages a nodecljs project with 'npm pack', suitable for installation or deployment to npmjs.org"
  [{:keys [target-path] :as project} & args]

  (let [workdir (io/file target-path "nodecljs")]
    (emit-packagejson project workdir)))
