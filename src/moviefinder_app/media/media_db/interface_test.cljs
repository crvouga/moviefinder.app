(ns moviefinder-app.media.media-db.interface-test
  (:require [cljs.test :refer-macros [deftest testing is async]]
            [cljs.spec.alpha :as s]
            [clojure.core.async :refer [go <!]]
            [moviefinder-app.media.media-db.interface :as interface]
            [moviefinder-app.media.media-db.backend]
            [moviefinder-app.backend.ctx :as ctx]))

(def implementations [{:tmdb/api-key (ctx/secrets :tmdb/api-key)
                       :media-db/impl :media-db-impl/tmdb-api}
                      #_{:media-db/impl :media-db-impl/fake}])

(deftest query-result-chan-test
  (testing "query-result-chan! response conforms to :query-result/response spec for all implementations"
    (async done
           (go
             (doseq [impl implementations]
               (let [query (merge impl
                                  {:query/limit 10
                                   :query/offset 0
                                   :query/select [:media/id :media/title]
                                   :query/where [:query/and
                                                 [:= :media/title "Test Movie"]]
                                   :query/order [[:media/title :asc]]})
                     result (<! (interface/query-result-chan! query))]

                 (is (s/valid? :query-result/query-result result)
                     (str "Invalid query result for implementation " (:media-db/impl impl) ": "
                          (s/explain-str :query-result/query-result result)))

                 (is (seq (:query-result/rows result))
                     (str "Query result should not be empty for implementation " (:media-db/impl impl)))))

             (done)))))

(deftest query-popular-movies-test
  (testing "query-result-chan! returns valid response for popular movies query"
    (async done
           (go
             (doseq [impl implementations]
               (let [query (merge impl
                                  {:query/limit 10
                                   :query/offset 0
                                   :query/select [:media/id :media/title :media/year
                                                  :media/popularity :media/genre-ids :media/poster-url]
                                   :query/where [:query/and
                                                 [:> :media/popularity 80]
                                                 [:= :media/media-type :media-type/movie]]
                                   :query/order [[:media/popularity :desc]]})
                     result (<! (interface/query-result-chan! query))]

                 (is (s/valid? :query-result/query-result result)
                     (str "Invalid query result for implementation " (:media-db/impl impl) ": "
                          (s/explain-str :query-result/query-result result)))

                 (is (sequential? (:query-result/rows result))
                     "Query result rows should be sequential")

                 (is (seq (:query-result/rows result))
                     (str "Query result should not be empty for implementation " (:media-db/impl impl)))

                 (when (seq (:query-result/rows result))
                   (is (>= (:media/popularity (first (:query-result/rows result))) 80)
                       "First result should have popularity greater than 80"))))

             (done)))))