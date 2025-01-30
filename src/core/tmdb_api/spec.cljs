(ns core.tmdb-api.spec
  (:require [cljs.spec.alpha :as s]))

(s/def :tmdb/id number?)
(s/def :tmdb/title string?)
(s/def :tmdb/release-date string?)
(s/def :tmdb/overview string?)
(s/def :tmdb/poster-path string?)
(s/def :tmdb/vote-average number?)
(s/def :tmdb/vote-count number?)
(s/def :tmdb/popularity number?)
(s/def :tmdb/movie
  (s/keys :opt [:tmdb/id
                :tmdb/title
                :tmdb/release-date
                :tmdb/overview
                :tmdb/poster-path
                :tmdb/vote-average
                :tmdb/vote-count
                :tmdb/popularity]))
(s/def :tmdb/page number?)
(s/def :tmdb/total-pages number?)
(s/def :tmdb/total-results number?)
(s/def :tmdb/results
  (s/coll-of :tmdb/movie))

(s/def :tmdb/response
  (s/keys :req [:tmdb/results
                :tmdb/page
                :tmdb/total-pages
                :tmdb/total-results]))
