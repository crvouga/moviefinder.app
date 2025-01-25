(ns moviefinder-app.media.media-db.impl-fake.impl
  (:require
   [moviefinder-app.media.media-db.interface :as media-db]
   [moviefinder-app.media.media-db.impl-fake.fake-data :refer [medias]]
   [clojure.core.async :refer [timeout go <!]]))

(defn to-query-result [q medias]
  (let [total (count medias)
        limit (-> q :query/limit (or 25))
        offset (-> q :query/offset (or 0))
        items (->> medias
                   (drop offset)
                   (take limit))]
    (-> q
        (merge {:query-result/query (select-keys q [:query/select :query/where :query/order :query/limit :query/offset])
                :query-result/limit limit
                :query-result/offset offset
                :query-result/total total
                :query-result/primary-key :media/id
                :query-result/rows items}))))

(defmethod media-db/query-chan! :media-db-impl/fake [q]
  (go
    (<! (timeout 100))
    (->> (to-query-result q medias))))
