(ns layer-demo.handler)

(def src-last-modified (atom nil))

(def sideload-url "https://tiuhti-web.s3.eu-west-1.amazonaws.com/src.zip")

(defn sideload []
  (let [t (Thread/currentThread)]
    (when-not (instance? clojure.lang.DynamicClassLoader (.getContextClassLoader t))
      (println "Installing DynamicClassLoader")
      (.setContextClassLoader t (clojure.lang.DynamicClassLoader. (.getContextClassLoader t)))))
  (swap! src-last-modified
         (fn [v]
           (let [src-url (java.net.URL. sideload-url)]
             (if (not v)
               (do
                 (println "First load")
                 (.addURL (.getContextClassLoader (Thread/currentThread)) src-url)
                 (.getLastModified (.openConnection src-url)))
               (let [url-connection (.openConnection src-url)
                     last-modified (.getLastModified url-connection)]
                 (when (> last-modified v)
                   (println "New source available, reloading")
                   (.setDefaultUseCaches url-connection false)
                   (require 'layer-demo.core :reload-all)
                   (println "reload done"))
                 last-modified))))))

(gen-class
  :name "layer_demo.handler"
  :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler
               org.crac.Resource]
  :post-init register-crac)

(defn -handleRequest [this in out ctx]
  (println "Running handler")
  (sideload)
  (let [get-clojure (requiring-resolve 'layer-demo.core/get-clojure)
        result (get-clojure)]
    (println result)
    (spit out result)))

;; crac stuff

(defn -register-crac [this]
  (.register (org.crac.Core/getGlobalContext) this))

(defn -beforeCheckpoint [this context]
  (println "Before checkpoint")
  (sideload)
  (requiring-resolve 'layer-demo.core/get-clojure)
  (println "Before checkpoint done"))

(defn -afterRestore [this context]
  (println "After restore")
  (println "After restore done"))
