(ns app.media.media-db.impl-tmdb-api.query-plan.discover-movie-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [app.media.media-db.impl-tmdb-api.query-plan :as query-plan]
   [lib.tmdb-api.shared :as shared]))

(defn to-query [limit offset]
  {:query/select
   [:media/id
    :media/title
    :media/year
    :media/popularity
    :media/genre-ids
    :media/poster-url],
   :query/where
   [:query/and
    [:> :media/popularity 80]
    [:= :media/media-type :media-type/movie]],
   :query/order [:media/popularity :desc],
   :query/limit limit,
   :query/offset offset,
   :media-db/impl :media-db/impl-tmdb-api})

(deftest query-plan-test
  (testing "discover movie"
    (is (= (query-plan/from-query (to-query 20 0))
           [[:tmdb-query-plan-item/discover-movie {:page/number 1
                                                   :page/size lib.tmdb-api.shared/page-size
                                                   :page/limit lib.tmdb-api.shared/page-size
                                                   :page/offset 0}]])))

  (testing "discover movie couple pages"
    (is (= (query-plan/from-query (to-query 40 0))
           [[:tmdb-query-plan-item/discover-movie {:page/number 1
                                                   :page/size lib.tmdb-api.shared/page-size
                                                   :page/limit lib.tmdb-api.shared/page-size
                                                   :page/offset 0}]
            [:tmdb-query-plan-item/discover-movie {:page/number 2
                                                   :page/size lib.tmdb-api.shared/page-size
                                                   :page/limit lib.tmdb-api.shared/page-size
                                                   :page/offset 0}]])))) 