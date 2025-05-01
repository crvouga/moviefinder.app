(ns lib.kv.namespaced)


(defn to-key [{:keys [:kv/namespace]} key]
  (vec (flatten [namespace key])))
