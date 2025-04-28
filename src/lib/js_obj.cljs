(ns lib.js-obj)

(defn init [x]
  (cond
    (keyword? x) (str x)
    (map? x) (reduce-kv (fn [m k v]
                          (let [k' (pr-str k)
                                v' (init v)]
                            (unchecked-set m k' v')
                            m))
                        #js {} x)
    (coll? x) (let [arr (array)]
                (doseq [x' x]
                  (.push arr (init x')))
                arr)
    :else (clj->js x)))