(ns lib.result)


(defn ok?
  "Check if the result is ok"
  [result]
  (= (:result/type result) :result/ok))

(defn err?
  "Check if the result is an error"
  [result] (= (:result/type result) :result/err))


(defn loading?
  "Check if the result is loading"
  [result]
  (= (:result/type result) :result/loading))

(def loading {:result/type :result/loading})

(def ok {:result/type :result/ok})

(def error {:result/type :result/err})


(defn merge-ok [& results]
  (reduce
   (fn [acc result] (if (ok? result) (merge acc result) (reduced result)))
   ok
   results))
