(ns app.media.media-db.impl-tmdb-api.impl
  (:require [clojure.core.async :refer [go <! go-loop]]
            [lib.tmdb-api.configuration]
            [lib.tmdb-api.discover-movie]
            [lib.tmdb-api.movie-details]
            [app.media.media-db.inter :as media-db]
            [app.media.media-db.impl-tmdb-api.query-plan-item.index]
            [app.media.media-db.impl-tmdb-api.query-plan-item :as query-plan-item]
            [app.media.media-db.impl-tmdb-api.query-plan :as query-plan]
            [lib.result :as result]))


(defn query-plan-query-results-chan! [ctx q-plan]
  (go-loop [q-results []
            q-plan q-plan]
    (if (empty? q-plan)
      q-results
      (let [q (<! (query-plan-item/result-chan! (first q-plan) ctx))]
        (recur (conj q-results q) (rest q-plan))))))

(defmethod media-db/query! :media-db/impl-tmdb-api [ctx q]
  (go
    (let [q-plan (query-plan/from-query q)
          q-results (<! (query-plan-query-results-chan! ctx q-plan))
          q-result (first q-results)]
      (-> q-result
          (merge result/ok)
          (assoc :query-result/query q)))))
