(ns layer-demo.handler)

(gen-class
  :name "layer_demo.handler"
  :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])

(defn -handleRequest [this in out ctx]
  (let [get-clojure (requiring-resolve 'layer-demo.core/get-clojure)]
    (println (get-clojure))
    (spit out (get-clojure))))
