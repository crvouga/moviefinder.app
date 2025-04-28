(ns lib.map-ext)


(defn map-keys-recursively [data key-fn]
  (cond
    (map? data)
    (->> data
         (map (fn [[k v]] [(key-fn k) (map-keys-recursively v key-fn)]))
         (into {}))

    (sequential? data)
    (mapv #(map-keys-recursively % key-fn) data)

    :else data))