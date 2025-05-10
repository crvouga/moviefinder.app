(ns app.media.media-db.impl-tmdb-api.query-plan.discover-movie-test
  (:require
   [app.media.media-db.impl-tmdb-api.query-plan :as query-plan]
   [clojure.test :refer [deftest is testing]]
   [lib.tmdb-api.shared :as shared]))

(defn to-query [limit offset]
  {:q/select
   [:media/id :media/title :media/year :media/popularity :media/genre-ids :media/poster-url],
   :q/where
   [:q/and
    [:q/> :media/popularity 80]
    [:q/= :media/media-type :media-type/movie]],
   :q/order [:media/popularity :desc],
   :q/limit limit,
   :q/offset offset})

(deftest query-plan-test
  (testing "discover movie"
    (is (= (query-plan/from-query (to-query lib.tmdb-api.shared/page-size 0))
           [[:tmdb-query-plan-item/discover-movie
             {:page/number 1
              :page/size lib.tmdb-api.shared/page-size
              :page/limit lib.tmdb-api.shared/page-size
              :page/offset 0}]])))

  (testing "discover movie couple pages"
    (is (= (query-plan/from-query (to-query (* lib.tmdb-api.shared/page-size 2) 0))
           [[:tmdb-query-plan-item/discover-movie
             {:page/number 1
              :page/size lib.tmdb-api.shared/page-size
              :page/limit lib.tmdb-api.shared/page-size
              :page/offset 0}]
            [:tmdb-query-plan-item/discover-movie
             {:page/number 2
              :page/size lib.tmdb-api.shared/page-size
              :page/limit lib.tmdb-api.shared/page-size
              :page/offset 0}]])))

  #_(testing "discover movie couple pages"
      (pprint/pprint (query-plan/from-query (to-query (- lib.tmdb-api.shared/page-size 5) 5)))

      (is (= (query-plan/from-query (to-query (- lib.tmdb-api.shared/page-size 5) 5))
             [[:tmdb-query-plan-item/discover-movie
               {:page/number 1
                :page/size lib.tmdb-api.shared/page-size
                :page/limit lib.tmdb-api.shared/page-size
                :page/offset 5}]
              [:tmdb-query-plan-item/discover-movie
               {:page/number 2
                :page/size lib.tmdb-api.shared/page-size
                :page/limit (- lib.tmdb-api.shared/page-size 5)
                :page/offset 0}]]))))

