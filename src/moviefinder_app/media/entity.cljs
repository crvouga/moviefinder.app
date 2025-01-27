(ns moviefinder-app.media.entity
  (:require [cljs.spec.alpha :as s]))

(s/def :media/id (s/or :numeric number?
                       :string string?))
(s/def :media/title string?)
(s/def :media/release-date string?)
(s/def :media/overview string?)
(s/def :media/poster-path string?)
(s/def :media/vote-average number?)
(s/def :media/vote-count number?)
(s/def :media/popularity number?)
(s/def :media/poster-url string?)
(s/def :media/backdrop-url string?)


(s/def :media/entity
  (s/keys :opt [:media/id
                :media/title
                :media/release-date
                :media/overview
                :media/poster-path
                :media/vote-average
                :media/vote-count
                :media/popularity
                :media/poster-url
                :media/backdrop-url]))
