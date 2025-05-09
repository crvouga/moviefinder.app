(ns lib.kv.impl-atom
  (:require
   [lib.kv.inter :as kv]
   [clojure.core.async :as a]
   [lib.kv.shared :refer [assoc-ok to-namespaced-key]]))

(def ^:private state! (atom {}))

(defmethod kv/init :kv/impl-atom [_] _)

(defmethod kv/get! :kv/impl-atom
  [inst key]
  (a/go
    (-> (get @state! (to-namespaced-key inst key))
        assoc-ok)))

(defmethod kv/set! :kv/impl-atom
  [inst key value]
  (a/go
    (swap! state! assoc (to-namespaced-key inst key) value)
    (assoc-ok value)))

(defmethod kv/zap! :kv/impl-atom
  [inst key]
  (a/go
    (swap! state! dissoc (to-namespaced-key inst key))
    (assoc-ok {})))
