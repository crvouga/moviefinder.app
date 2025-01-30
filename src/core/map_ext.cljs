(ns core.map-ext)

(defn convert-keys-recursively [data key-fn]
  (cond
    (nil? data) nil

    (map? data)
    (->> data
         (map (fn [[k v]] [(key-fn k) (convert-keys-recursively v key-fn)]))
         (into {}))

    (sequential? data)
    (mapv #(convert-keys-recursively % key-fn) data)

    :else data))