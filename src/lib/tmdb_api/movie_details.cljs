;; https://developer.themoviedb.org/reference/movie-details
(ns lib.tmdb-api.movie-details
  (:require
   [clojure.core.async :refer [<! go]]
   [lib.http-client :as http-client]
   [lib.tmdb-api.shared :as shared]
   [lib.tmdb-api.shared-spec]))

(defn fetch!
  "Returns a channel containing a :tmdb/response"
  [movie-id params]
  (go
    (-> params
        (shared/build-request (str "/movie/" movie-id))
        http-client/fetch!
        <!
        shared/map-response)))