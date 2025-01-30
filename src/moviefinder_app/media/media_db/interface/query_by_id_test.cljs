(ns moviefinder-app.media.media-db.interface.query-by-id-test
  (:require [cljs.test :refer-macros [deftest testing is async]]
            [clojure.core.async :refer [go <!]]
            [moviefinder-app.media.media-db.interface :as interface]
            [moviefinder-app.media.media-db.impl-pg.impl]
            [core.db-conn.impl]
            [core.db-conn.interface :as db-conn]))

(def db (db-conn/new! {:db-conn/impl :db-conn-impl/pglite}))

(def configs [(merge db
                     {:media-db/impl :media-db-impl/db-conn})])

(def test-media {:media/id "test-id"
                 :media/title "Test Movie"
                 :media/release-date "2023-01-01"
                 :media/overview "Test overview"
                 :media/poster-path "/test.jpg"
                 :media/vote-average 8.5
                 :media/vote-count 100
                 :media/popularity 50.0
                 :media/poster-url "http://test.com/poster.jpg"
                 :media/backdrop-url "http://test.com/backdrop.jpg"})

(deftest put-and-query-test
  (testing "Can put and query media using pg implementation"
    (async done
           (go
             (doseq [config configs]
               (let [put-result (<! (interface/put-chan! (assoc config :media/entity test-media)))
                     query (merge config
                                  {:query/limit 10
                                   :query/offset 0
                                   :query/where [:= :media/id "test-id"]})
                     query-result (<! (interface/query-result-chan! query))]

                 (is (= :result/ok (:result/type put-result))
                     "Put operation should succeed")

                 (is (= 1 (count (:query-result/rows query-result)))
                     "Should find exactly one result")

                 (is (= test-media (first (:query-result/rows query-result)))
                     "Retrieved media should match inserted media")))
             (done)))))
