(ns lib.kv.namespaced)


(defn to-key [{:keys [:kv/namespace]} key]
  (if (keyword? namespace)
    (keyword (str namespace ":" key))
    key))
