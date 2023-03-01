(ns build
  (:require [clojure.tools.build.api :as b]
            [clojure.java.io :as io]
            [babashka.fs :as fs]))

(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def lambda-file "target/lambda.jar")

(def layer-file "target-layer/layer.zip")
(def layer-zip-dir "target-layer/layer")

(defn lambda [_]
  (b/delete {:path "target"})
  (b/copy-dir {:src-dirs ["src"]
               :target-dir class-dir})
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :ns-compile ['layer-demo.handler]
                  :class-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file lambda-file}))

(defn layer [_]
  (b/delete {:path "target-layer"})
  (fs/create-dirs (fs/file layer-zip-dir "java" "lib"))
  (doseq [[_ {:keys [paths]}] (-> basis :libs)]
    (doseq [path paths]
      (fs/copy path (fs/file layer-zip-dir "java" "lib" (fs/file-name path)))))
  (b/zip {:src-dirs [layer-zip-dir]
          :zip-file layer-file}))
