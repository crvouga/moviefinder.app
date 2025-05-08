(ns lib.kv.impl-atom
  (:require
   [lib.kv.inter :as kv]
   [clojure.core.async :as a]
   [lib.kv.shared :refer [assoc-ok to-key]]))

(defmethod kv/new! :kv/impl-atom
  [config]
  (merge config {::state! (atom {})}))


(defmethod kv/get! :kv/impl-atom
  [{:keys [::state!] :as inst} key]
  (a/go
    (-> (get @state! (to-key inst key))
        assoc-ok)))

(defmethod kv/set! :kv/impl-atom
  [{:keys [::state!] :as inst} key value]
  (a/go
    (swap! state! assoc (to-key inst key) value)
    (assoc-ok value)))

(defmethod kv/zap! :kv/impl-atom
  [{:keys [::state!] :as inst} key]
  (a/go
    (swap! state! dissoc (to-key inst key))
    (assoc-ok {})))
