(ns app.feed.feed
  (:require
   [cljs.spec.alpha :as s]
   [lib.time :as time]))

(s/def :feed/feed-id string?)
(s/def :feed/tags some?)
(s/def :feed/created-at string?)
(s/def :feed/updated-at string?)

(defn create []
  (let [created-at (time/now!)]
    {:feed/feed-id (str (random-uuid))
     :feed/tags []
     :feed/created-at created-at
     :feed/updated-at created-at}))

(defn id [feed]
  (:feed/feed-id feed))