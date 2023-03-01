(ns layer-demo.core
  (:require [clj-http.client :as http]))

(defn get-clojure []
  (-> (http/get "https://clojure.org") :body (subs 0 40)))
