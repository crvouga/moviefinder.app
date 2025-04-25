(ns app.media.media-db.interface.query-by-id-test
  (:require [cljs.test :refer-macros [deftest testing is async]]
            [clojure.core.async :refer [go <!]]
            [app.media.media-db.interface :as media-db]
            [app.media.media-db.interface.fixture :as fixture]))

(deftest query-by-id-test
  (testing "Can query media by id"
    (async done
           (go
             (doseq [config fixture/configs-read-only]
               (let [test-media (assoc fixture/test-media :media/id "123")
                     _put-result (<! (media-db/upsert-chan! (assoc config :media/entity test-media)))
                     query (merge config
                                  {:query/limit 1
                                   :query/offset 0
                                   :query/where [:= :media/id "123"]})
                     result (<! (media-db/query-result-chan! query))
                     first-result (first (:query-result/rows result))]

                 (is (= "123" (-> first-result :media/id str))
                     (str "Media ID should match for implementation " (:media-db/impl config)))))
             (done)))))
