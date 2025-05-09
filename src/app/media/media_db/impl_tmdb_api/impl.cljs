(ns app.media.media-db.impl-tmdb-api.impl
  (:require [clojure.core.async :refer [go <! go-loop]]
            [lib.tmdb-api.configuration]
            [lib.tmdb-api.discover-movie]
            [lib.tmdb-api.movie-details]
            [app.media.media-db.inter :as media-db]
            [app.media.media-db.impl-tmdb-api.query-plan-item.index]
            [app.media.media-db.impl-tmdb-api.query-plan-item :as query-plan-item]
            [lib.result :as result]))

(defn to-query-plan-items [q]
  (let [where (:query/where q)]
    (cond
      (nil? where)
      [[:tmdb-query-plan-item/discover-movie q]]

      (and (vector? where)
           (= := (first where))
           (= :media/id (second where)))
      [[:tmdb-query-plan-item/movie-details q]]

      (and (vector? where)
           (= :or (first where)))
      (mapcat #(to-query-plan-items (assoc q :query/where %)) (rest where))

      :else
      [[:tmdb-query-plan-item/discover-movie q]])))


(defn query-plan-query-results-chan! [ctx q-plan-items]
  (go-loop [q-results []
            q-plan-items q-plan-items]
    (if (empty? q-plan-items)
      q-results
      (let [q (<! (query-plan-item/query-result-chan! (first q-plan-items) ctx))]
        (recur (conj q-results q) (rest q-plan-items))))))

(defmethod media-db/query! :media-db/impl-tmdb-api [ctx q]
  (go
    (let [query-plan-items (to-query-plan-items q)
          query-results (<! (query-plan-query-results-chan! ctx query-plan-items))
          query-result (first query-results)]
      (-> query-result
          (merge result/ok)
          (assoc :query-result/query q)))))
