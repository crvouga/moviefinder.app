(ns lib.kv.impl-namespaced
  (:require
   [lib.kv.interface :as kv]
   [clojure.core.async :as a]))

(defmethod kv/new! :kv/impl-namespaced
  [config]
  config)

(defn to-namespaced-key [namespace key]
  (keyword (str namespace ":" key)))

(defmethod kv/get! :kv/impl-namespaced
  [{:keys [:kv/namespace] :as inst} key]
  (a/go
    (kv/get! inst (to-namespaced-key namespace key))))

(defmethod kv/set! :kv/impl-namespaced
  [{:keys [:kv/namespace] :as inst} key value]
  (a/go
    (kv/set! inst (to-namespaced-key namespace key) value)))

(defmethod kv/zap! :kv/impl-namespaced
  [{:keys [:kv/namespace] :as inst} key]
  (a/go
    (kv/zap! inst (to-namespaced-key namespace key))))
