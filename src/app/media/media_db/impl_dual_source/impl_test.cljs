#_(ns app.media.media-db.impl-dual-source.impl-test
    (:require [cljs.test :refer-macros [deftest testing is async]]
              [clojure.core.async :refer [go <! timeout]]
              [app.media.media-db.interface :as media-db]
              [app.media.media-db.interface.fixture :as fixture]))

#_(deftest dual-source-test
    (testing "Can query media from dual sources with db-conn primary and tmdb secondary"
      (async done
             (go
               (let [primary-config fixture/config-tmdb-api
                     secondary-config fixture/config-tmdb-api
                     test-media (assoc fixture/test-media :media/id "123")
                     dual-source-config {:media-db/impl :media-db-impl/dual-source
                                         :media-db-impl-dual-source/primary primary-config
                                         :media-db-impl-dual-source/secondary secondary-config}
                     _put-result (<! (media-db/upsert-chan! (assoc primary-config :media/entity test-media)))
                     query (merge dual-source-config
                                  {:query/limit 1
                                   :query/offset 0
                                   :query/where [:= :media/id "123"]})
                     result (<! (media-db/query-result-chan! query))
                     first-result (first (:query-result/rows result))]

                 (is (= "123" (-> first-result :media/id str))
                     "Media ID should match in dual source implementation"))
               (done)))))

#_(deftest did-put-results-from-secondary-into-primary-test
    (testing "Results from secondary source are stored in primary after dual source query"
      (async done
             (go
               (let [primary-config fixture/config-tmdb-api
                     secondary-config fixture/config-tmdb-api
                     test-id "123" ; Using a different ID that won't exist in primary
                     dual-source-config {:media-db/impl :media-db-impl/dual-source
                                         :media-db-impl-dual-source/primary primary-config
                                         :media-db-impl-dual-source/secondary secondary-config}
                   ; First verify it exists in secondary
                     secondary-query (merge secondary-config
                                            {:query/limit 1
                                             :query/offset 0
                                             :query/where [:= :media/id test-id]})
                     secondary-result (<! (media-db/query-result-chan! secondary-query))
                     secondary-item (first (:query-result/rows secondary-result))
                     _ (is (some? secondary-item)
                           "Test ID should exist in secondary source")

                   ; Then query through dual source
                     dual-query (merge dual-source-config
                                       {:query/limit 1
                                        :query/offset 0
                                        :query/where [:= :media/id test-id]})
                     _dual-result (<! (media-db/query-result-chan! dual-query))

                   ; Add a timeout to allow time for secondary results to be stored
                     _ (<! (timeout 1000))

                   ; Then query primary directly to check if data was stored
                     primary-query (merge primary-config
                                          {:query/limit 1
                                           :query/offset 0
                                           :query/where [:= :media/id test-id]})
                     primary-result (<! (media-db/query-result-chan! primary-query))
                     stored-result (first (:query-result/rows primary-result))]

                 (is (some? stored-result)
                     "Media from secondary source should be stored in primary")
                 (is (= test-id (-> stored-result :media/id str))
                     "Stored media ID should match in primary source"))
               (done)))))
