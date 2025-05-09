(ns app.media.media-db.impl-tmdb-api.mapping
  (:require
   [app.media.media-id :as media-id]
   [clojure.set :refer [rename-keys]]
   [lib.tmdb-api.configuration]
   [lib.tmdb-api.discover-movie]
   [lib.tmdb-api.movie-details]))

(def key-mapping {:tmdb/id :media/tmdb-id
                  :tmdb/title :media/title
                  :tmdb/release-date :media/release-date
                  :tmdb/overview :media/overview
                  :tmdb/poster-path :media/poster-path
                  :tmdb/backdrop-path :media/backdrop-path
                  :tmdb/vote-average :media/vote-average
                  :tmdb/vote-count :media/vote-count
                  :tmdb/popularity :media/popularity})

(defn- tmdb-item->media [item]
  (-> item
      (rename-keys key-mapping)
      (select-keys (vals key-mapping))
      (assoc :media/id (media-id/from-tmdb-id (:tmdb/id item)))))

(defn to-poster-url [tmdb-config media]
  (lib.tmdb-api.configuration/to-poster-url tmdb-config (:media/poster-path media)))

(defn to-backdrop-url [tmdb-config media]
  (lib.tmdb-api.configuration/to-backdrop-url tmdb-config (:media/backdrop-path media)))

(defn- assoc-image-urls [tmdb-config media]
  (-> media
      (assoc :media/poster-url (to-poster-url tmdb-config media))
      (assoc :media/backdrop-url (to-backdrop-url tmdb-config media))))

(defn tmdb-result->query-result
  [{:keys [page/limit page/offset tmdb/total-results tmdb/results] :as input}]
  (let [items (->> results
                   (map tmdb-item->media)
                   (map #(assoc-image-urls input %))
                   (drop (or offset 0))
                   (take (or limit 25)))]
    {:query-result/query (select-keys input [:q/where :q/limit :q/offset :q/order :q/select])
     :query-result/limit limit
     :query-result/offset offset
     :query-result/total total-results
     :query-result/primary-key :media/id
     :query-result/rows items}))


