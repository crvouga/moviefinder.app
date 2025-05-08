(ns lib.kv.shared
  (:require
   [lib.result :as result]))

(defn assoc-ok [value]
  (if (map? value)
    (merge value result/ok)
    (merge {:kv/value value} result/ok)))


(defn to-key [{:keys [:kv/namespace]} key]
  (vec (flatten [namespace key])))
