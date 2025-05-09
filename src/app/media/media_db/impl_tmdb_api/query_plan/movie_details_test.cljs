(ns app.media.media-db.impl-tmdb-api.query-plan.movie-details-test
  (:require
   [app.media.media-db.impl-tmdb-api.query-plan :as query-plan]
   [app.media.media-id :as media-id]
   [clojure.test :refer [deftest is testing]]
   [lib.tmdb-api.shared]))

(def tmdb-id lib.tmdb-api.shared/movie-id-fight-club)
(def media-id (media-id/from-tmdb-id tmdb-id))

(deftest query-plan-test
  (testing "movie details"
    (let [q {:q/select [:media/id],
             :q/where [:q/= :media/id media-id]}]
      (is (= (query-plan/from-query q)
             [[:tmdb-query-plan-item/movie-details {:tmdb/id tmdb-id}]]))))


  (testing "movie details with and"
    (let [q {:q/select [:media/id],
             :q/where [:q/and [:q/= :media/id media-id]]}]
      (is (= (query-plan/from-query q)
             [[:tmdb-query-plan-item/movie-details {:tmdb/id tmdb-id}]]))))


  (testing "movie details with or"
    (let [q {:q/select [:media/id],
             :q/where [:q/or [:q/= :media/id media-id]]}]
      (is (= (query-plan/from-query q)
             [[:tmdb-query-plan-item/movie-details {:tmdb/id tmdb-id}]]))))) 