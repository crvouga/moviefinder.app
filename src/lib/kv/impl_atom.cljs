(ns lib.kv.impl-atom
  (:require
   [lib.kv.inter :as kv]
   [clojure.core.async :as a]
   [lib.kv.namespaced :as namespaced]))

(defmethod kv/new! :kv/impl-atom
  [config]
  (merge config {::state! (atom {})}))

(defmethod kv/get! :kv/impl-atom
  [{:keys [::state!] :as inst} key]
  (a/go
    (-> (get @state! (namespaced/to-key inst key))
        (assoc :result/type :result/ok))))

(defmethod kv/set! :kv/impl-atom
  [{:keys [::state!] :as inst} key value]
  (a/go
    (swap! state! assoc (namespaced/to-key inst key) value)
    (assoc value :result/type :result/ok)))

(defmethod kv/zap! :kv/impl-atom
  [{:keys [::state!] :as inst}]
  (a/go
    (swap! state! dissoc (namespaced/to-key inst key))
    (assoc {} :result/type :result/ok)))
