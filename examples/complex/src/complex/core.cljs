(ns complex.core
  (:require [cljs.nodejs :as nodejs]
            [promesa.core :as p :include-macros true]))

(nodejs/enable-util-print!)

(def pb (nodejs/require "protobufjs"))
(def homedir (nodejs/require "homedir"))

(def builder (.newBuilder pb))

(defn- loadproto [name]
  (do
    (.loadProtoFile pb (str "./" name ".proto") builder)
    (.build builder name)))

(def init (loadproto "appinit"))

(defn greeting []
  (p/promise
   (fn [resolve reject]
     (resolve "Hello world"))))

(defn -main [& args]
  (-> (greeting)
      (p/then println)))

(set! *main-cli-fn* -main)
