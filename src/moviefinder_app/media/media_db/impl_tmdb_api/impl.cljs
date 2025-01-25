(ns moviefinder-app.media.media-db.impl-tmdb-api.impl
  (:require
   [clojure.core.async :refer [<! go timeout]]
   [core.tmdb-api.discover.movie]
   [moviefinder-app.media.media-db.interface :as media-db]))



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

(defmethod media-db/query-chan! :media-db-impl/tmdb-api [q]
  (go
    (let [medias (<! (core.tmdb-api.discover.movie/fetch-chan {}))]
      (->> (to-query-result q medias)))))
