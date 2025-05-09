(ns app.media.media-db.impl-tmdb-api.query-plan
  (:require
   [app.media.media-id :as media-id]
   [lib.query :as query]
   [lib.tmdb-api.shared :as shared]))

(defn- to-discover-movie-query-plan-items [q]
  (->> (query/to-pages q lib.tmdb-api.shared/page-size)
       (mapv #(vector :tmdb-query-plan-item/discover-movie %))))

(defn- to-movie-details-query-plan-items [_q media-id]
  [[:tmdb-query-plan-item/movie-details {:tmdb/id (media-id/to-tmdb-id media-id)}]])


(defn from-query [{:keys [query/where] :as q}]
  (cond
    (nil? where)
    (to-discover-movie-query-plan-items q)

    (and (vector? where)
         (= := (first where))
         (= :media/id (second where)))
    (to-movie-details-query-plan-items q (nth where 2))

    (and (vector? where)
         (= :or (first where)))
    (mapcat #(from-query (assoc q :query/where %)) (rest where))

    :else
    (to-discover-movie-query-plan-items q)))