(ns moviefinder-app.media.media-db.impl-fake.impl
  (:require
   [moviefinder-app.media.media-db.interface :as media-db]
   [moviefinder-app.media.media-db.impl-fake.movies :refer [movies]]))

(defn ->limit [q] (-> q :query/limit (or 25)))
(defn ->offset [q] (-> q :query/offset (or 0)))

(defmethod media-db/query :media-db-impl/fake [q]
  (merge q {:paginated/total (count movies)
            :paginated/items (->> movies
                                  (take (->limit q))
                                  (drop (->offset q)))}))
