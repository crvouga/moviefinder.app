(ns app.kv.impl-rpc.frontend
  (:require
   [app.rpc.frontend :as rpc]
   [lib.kv.inter :as kv]))

(defmethod kv/init :kv/impl-rpc [inst] inst)

(defmethod kv/get! :kv/impl-rpc [inst key]
  (rpc/call! [:rpc-fn/kv-get inst key]))

(defmethod kv/set! :kv/impl-rpc [inst key value]
  (rpc/call! [:rpc-fn/kv-put inst key value]))

(defmethod kv/zap! :kv/impl-rpc [inst key]
  (rpc/call! [:rpc-fn/kv-zap inst key]))

