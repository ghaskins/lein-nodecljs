(ns leiningen.pack
  (:require [clojure.java.io :as io]
            [cheshire.core :as json]))

(defn- truncate-file [filename content]

  ;; ensure the path exists
  (io/make-parents filename)

  ;; and blast it out to the filesystem
  (spit filename content :truncate true))


(defn pack
  "Packages a nodecljs project with 'npm pack', suitable for installation or deployment to npmjs.org"
  [{:keys [name description version url target-path npm] :as project} & args]

  ;; Generate our package.json
  (let [path (io/file target-path "package.json")
        content (json/generate-string {:name name
                                       :description description
                                       :version version
                                       :homepage url
                                       :dependencies (:dependencies npm)
                                       :bin {name "./main.js"}}
                                      {:pretty true})]
    (truncate-file path content)))
