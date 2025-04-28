(ns lib.kv.impl-atom
  (:require
   [lib.kv.interface :as kv]
   [clojure.core.async :as a]))

(defmethod kv/new! :kv/impl-atom
  [config]
  (merge config {::state! (atom {})}))

(defmethod kv/get! :kv/impl-atom
  [{:keys [::state!]} key]
  (a/go
    (get @state! key)))

(defmethod kv/set! :kv/impl-atom
  [{:keys [::state!]} key value]
  (a/go
    (swap! state! assoc key value)))

(defmethod kv/zap! :kv/impl-atom
  [{:keys [::state!]}]
  (a/go
    (swap! state! dissoc key)))
