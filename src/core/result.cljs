(ns core.result)


(defn ok? [result]
  (= (:result/type result) :result/ok))

(defn error? [result]
  (= (:result/type result) :result/error))


(defn loading? [result]
  (= (:result/type result) :result/loading))

(def loading {:result/type :result/loading})

(def ok {:result/type :result/ok})

(def error {:result/type :result/error})