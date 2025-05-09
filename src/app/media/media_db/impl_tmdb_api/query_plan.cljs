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

(defn- from-query-recursively [{:keys [q/where] :as q}]
  (cond
    (nil? where)
    (to-discover-movie-query-plan-items q)

    (and (vector? where)
         (= :q/= (first where))
         (= :media/id (second where)))
    (to-movie-details-query-plan-items q (nth where 2))

    (and (vector? where) (= :q/or (first where)))
    (mapcat #(from-query-recursively (assoc q :q/where %)) (rest where))

    (and (vector? where) (= :q/and (first where)))
    (mapcat #(from-query-recursively (assoc q :q/where %)) (rest where))

    :else
    (to-discover-movie-query-plan-items q)))

(defn from-query [q]
  (-> q from-query-recursively distinct))