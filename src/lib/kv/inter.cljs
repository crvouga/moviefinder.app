(ns lib.kv.inter)

(defmulti init (fn [inst] (get inst :kv/impl)))
(defmulti get! (fn [inst _key] (get inst :kv/impl)))
(defmulti set! (fn [inst _key _value] (get inst :kv/impl)))
(defmulti zap! (fn [inst _key] (get inst :kv/impl)))

