(ns build
  (:require [clojure.tools.build.api :as b]
            [babashka.fs :as fs]))

(def basis (b/create-basis {:project "deps.edn"}))

(defn lambda [_]
  (b/delete {:path "target"})
  ;; Leave out source, let sideloader handle
  #_(b/copy-dir {:src-dirs ["src"]
               :target-dir "target/classes"})
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  ;; Compile only handler namespace
                  :ns-compile ['layer-demo.handler]
                  :class-dir "target/classes"})
  (b/jar {:class-dir "target/classes"
          :jar-file "target/lambda.jar"}))

(defn layer [_]
  (b/delete {:path "target-layer"})
  ;; Compile everything in order to get library Clojure code compiled to bytecode
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir "target-layer/classes"})
  (b/uber {:uber-file "target-layer/java/lib/layer.jar"
           :class-dir "target-layer/classes"
           :basis basis
           ;; Exclude application
           :exclude [#"layer_demo.*"]})
  (fs/zip "target-layer/layer.zip" "target-layer/java/lib" {:root "target-layer"}))

(defn src [_]
  (fs/zip "target/src.zip" "src" {:root "src"}))
