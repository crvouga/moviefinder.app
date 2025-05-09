;; https://developer.themoviedb.org/reference/discover-movie
(ns lib.tmdb-api.discover-movie
  (:require
   [clojure.core.async :refer [<! go]]
   [lib.http-client :as http-client]
   [lib.tmdb-api.shared :as shared]
   [lib.tmdb-api.shared-spec :as shared-spec]))

(defn fetch!
  "Returns a channel containing a :tmdb/response"
  [params]
  (go
    (-> params
        (shared/build-request "/discover/movie")
        http-client/fetch!
        <!
        (shared/map-response  shared-spec/tmdb-results-empty))))