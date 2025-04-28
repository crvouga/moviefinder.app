(ns lib.kv.inter)

(defmulti new! :kv/impl)
(defmulti get! :kv/impl)
(defmulti set! :kv/impl)
(defmulti zap! :kv/impl)



