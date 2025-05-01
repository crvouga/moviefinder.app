#_(ns app.media.media-db.inter.put-then-query-by-id-test
    (:require [cljs.test :refer-macros [deftest testing is async]]
              [clojure.core.async :refer [go <!]]
              [app.media.media-db.inter :as media-db]
              [app.media.media-db.inter.fixture :as fixture]))


#_(deftest put-and-query-test
    (testing "Can put and query media by id"
      (async done
             (go
               (doseq [config fixture/configs]
                 (let [put-result (<! (media-db/upsert-chan! (assoc config :media/entity fixture/test-media)))
                       query (merge config
                                    {:query/limit 10
                                     :query/offset 0
                                     :query/where [:= :media/id "test-id"]})
                       query-result (<! (media-db/query-result-chan! config query))]

                   (is (= :result/ok (:result/type put-result))
                       "Put operation should succeed")

                   (is (= 1 (count (:query-result/rows query-result)))
                       "Should find exactly one result")

                   (is (-> query-result
                           :query-result/rows
                           first
                           (select-keys (keys fixture/test-media))
                           (= fixture/test-media))
                       "Retrieved media should match inserted media")))
               (done)))))
