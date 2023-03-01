(ns layer-demo.handler)

(gen-class
  :name "layer_demo.handler"
  :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler
               org.crac.Resource]
  :post-init register-crac)

(defn -handleRequest [this in out ctx]
  (println "Running handler")
  (let [get-clojure (requiring-resolve 'layer-demo.core/get-clojure)]
    (println (get-clojure))
    (spit out (get-clojure))))

;; crac stuff

(defn -register-crac [this]
  (.register (org.crac.Core/getGlobalContext) this))

(defn -beforeCheckpoint [this context]
  (println "Before checkpoint")
  ((requiring-resolve 'layer-demo.core/get-clojure))
  (println "Before checkpoint done"))

(defn -afterRestore [this context]
  (println "After restore"))
