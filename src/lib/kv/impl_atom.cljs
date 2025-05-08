(ns lib.kv.impl-atom
  (:require
   [lib.kv.inter :as kv]
   [clojure.core.async :as a]
   [lib.kv.namespaced :as namespaced]
   [lib.result :as result]))

(defmethod kv/new! :kv/impl-atom
  [config]
  (merge config {::state! (atom {})}))

(defn- assoc-ok [value]
  (if (map? value)
    (merge value result/ok)
    (merge {:kv/value value} result/ok)))

(defmethod kv/get! :kv/impl-atom
  [{:keys [::state!] :as inst} key]
  (a/go
    (-> (get @state! (namespaced/to-key inst key))
        assoc-ok)))

(defmethod kv/set! :kv/impl-atom
  [{:keys [::state!] :as inst} key value]
  (a/go
    (swap! state! assoc (namespaced/to-key inst key) value)
    (assoc-ok value)))

(defmethod kv/zap! :kv/impl-atom
  [{:keys [::state!] :as inst} key]
  (a/go
    (swap! state! dissoc (namespaced/to-key inst key))
    (assoc-ok {})))
