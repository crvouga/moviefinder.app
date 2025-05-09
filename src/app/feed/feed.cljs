(ns app.feed.feed
  (:require
   [cljs.spec.alpha :as s]
   [lib.time :as time]))

(s/def :feed/feed-id string?)
(s/def :feed/tags some?)
(s/def :feed/created-at string?)
(s/def :feed/updated-at string?)

(defn default []
  {:feed/feed-id (str (random-uuid))
   :feed/tags []
   :feed/created-at (time/now!)})

(defn id [feed]
  (:feed/feed-id feed))

(defn to-media-query [feed]
  {:query/select [:media/id
                  :media/title
                  :media/year
                  :media/popularity
                  :media/genre-ids
                  :media/poster-url]
   :query/where [:query/and
                 [:> :media/popularity 80]
                 [:= :media/media-type :media-type/movie]]
   :query/order [:media/popularity :desc]
   :query/limit 25
   :query/offset (-> feed :feed/start-index (or 0))})

(defn valid? [feed]
  (s/valid? :feed/feed feed))
