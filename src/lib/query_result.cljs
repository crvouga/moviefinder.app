(ns lib.query-result
  (:require
   [clojure.spec.alpha :as s]))

(def query-result-keys
  [:query-result/limit
   :query-result/offset
   :query-result/total
   :query-result/primary-key])


(s/def :query-result/query (s/keys :opt [:q/select :q/where :q/order :q/limit :q/offset]))
(s/def :query-result/limit number?)
(s/def :query-result/offset number?)
(s/def :query-result/total number?)
(s/def :query-result/primary-key keyword?)
(s/def :query-result/rows (s/coll-of map?))

(s/def :query-result/query-result
  (s/keys :req [:query-result/query
                :query-result/limit
                :query-result/offset
                :query-result/total
                :query-result/primary-key
                :query-result/rows]))

(def init
  {:query-result/limit 25
   :query-result/offset 0
   :query-result/total 0
   :query-result/rows []})


(defn- combine-reducer [acc query-result]
  (let [rows (concat (:query-result/rows acc) (:query-result/rows query-result))
        total (max (:query-result/total acc) (:query-result/total query-result))
        limit (:query-result/limit acc)
        offset (:query-result/offset acc)
        primary-key (:query-result/primary-key acc)]
    {:query-result/limit limit
     :query-result/offset offset
     :query-result/total total
     :query-result/primary-key primary-key
     :query-result/rows rows}))

(defn combine [acc results]
  (reduce combine-reducer acc results))


(defn subset?
  [smaller-result bigger-result]
  (let [rows-smaller (:query-result/rows smaller-result)
        rows-bigger (:query-result/rows bigger-result)
        limit-smaller (:query-result/limit smaller-result)
        offset-smaller (:query-result/offset smaller-result)]
    (= rows-smaller (->> rows-bigger (drop offset-smaller) (take limit-smaller)))))

