(ns app.kv.impl-rpc.backend
  (:require
   [app.rpc.backend :as rpc]
   [lib.kv.inter :as kv]))



(rpc/reg-fn
 :rpc-fn/kv-get
 (fn [ctx inst key]
   (kv/get! (merge ctx inst) key)))

(rpc/reg-fn
 :rpc-fn/kv-set
 (fn [ctx inst key value]
   (kv/set! (merge ctx inst) key value)))

(rpc/reg-fn
 :rpc-fn/kv-zap
 (fn [ctx inst key]
   (kv/zap! (merge ctx inst) key)))