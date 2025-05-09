(ns lib.query-result
  (:require
   [clojure.spec.alpha :as s]))

(def query-result-keys
  [:query-result/limit
   :query-result/offset
   :query-result/total
   :query-result/primary-key])


(s/def :query-result/query (s/keys :opt [:query/select :query/where :query/order :query/limit :query/offset]))
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