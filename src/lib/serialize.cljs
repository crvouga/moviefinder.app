(ns lib.serialize
  (:require [clojure.walk :as walk]))


(defn serializable? [x]
  (or (nil? x)
      (string? x)
      (number? x)
      (boolean? x)
      (keyword? x)
      (symbol? x)
      (and (coll? x)
           (every? serializable? x))))

(defn assert-serializable [x]
  (walk/postwalk
   (fn [node]
     (assert (serializable? node) (str "Non-serializable value: " node x)))
   x))