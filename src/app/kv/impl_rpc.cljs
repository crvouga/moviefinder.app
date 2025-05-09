(ns app.kv.impl-rpc
  (:require
   [lib.kv.inter :as kv]))



(defmethod kv/init :kv/impl-rpc [inst] inst)