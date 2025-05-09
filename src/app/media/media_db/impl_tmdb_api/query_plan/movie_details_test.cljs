(ns app.media.media-db.impl-tmdb-api.query-plan.movie-details-test
  (:require
   [app.media.media-db.impl-tmdb-api.query-plan :as query-plan]
   [app.media.media-id :as media-id]
   [clojure.test :refer [deftest is testing]]
   [lib.tmdb-api.shared]))

(defn to-query [media-id]
  {:query/select
   [:media/id
    :media/title
    :media/year
    :media/popularity
    :media/genre-ids
    :media/poster-url],
   :query/where [:= :media/id media-id]})

(deftest query-plan-test
  (testing "movie details"
    (is (= (query-plan/from-query (to-query (media-id/from-tmdb-id lib.tmdb-api.shared/movie-id-fight-club)))
           [[:tmdb-query-plan-item/movie-details {:tmdb/id lib.tmdb-api.shared/movie-id-fight-club}]])))) 