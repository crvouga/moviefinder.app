(ns lib.result)


(defn ok? [result]
  (= (:result/type result) :result/ok))

(defn err? [result]
  (= (:result/type result) :result/err))


(defn loading? [result]
  (= (:result/type result) :result/loading))

(def loading {:result/type :result/loading})

(def ok {:result/type :result/ok})

(def error {:result/type :result/err})
