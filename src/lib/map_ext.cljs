(ns lib.map-ext
  (:require
   [clojure.set :refer [rename-keys]]))


(defn map-keys-recursively
  "Recursively maps the keys of a map using the key-fn."
  [data key-fn]
  (cond
    (map? data)
    (->> data
         (map (fn [[k v]] [(key-fn k) (map-keys-recursively v key-fn)]))
         (into {}))

    (sequential? data)
    (mapv #(map-keys-recursively % key-fn) data)

    :else data))

(defn inverse
  "Returns a map with the keys and values swapped."
  [m]
  (reduce (fn [acc [k v]] (assoc acc v k)) {} m))

(defn rename-keys-select
  "Returns a map with the keys and values swapped."
  [m kmap]
  (-> m
      (select-keys (keys kmap))
      (rename-keys kmap)))
