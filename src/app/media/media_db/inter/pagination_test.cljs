(ns app.media.media-db.inter.pagination-test
  (:require [cljs.test :refer-macros [deftest testing is async]]
            [clojure.core.async :refer [go <!]]
            [app.media.media-db.inter :as interface]
            [app.media.media-db.backend]
            [app.media.media-db.inter.fixture :as fixture]))

(deftest pagination-test
  (testing "query-result-chan! returns correct paginated subsets"
    (async done
           (go
             (doseq [config fixture/configs-read-only]
               (let [full-query (merge config {:query/limit 20 :query/offset 0})
                     query1 (merge config {:query/limit 5 :query/offset 0})
                     query2 (merge config {:query/limit 5 :query/offset 5})
                     full-result (<! (interface/query-result-chan! full-query))
                     result1 (<! (interface/query-result-chan! query1))
                     result2 (<! (interface/query-result-chan! query2))
                     full-rows (:queried/rows full-result)]

                 ; Test paginated results are subsequences of full result
                 (is (= (:queried/rows result1)
                        (take 5 full-rows))
                     "First page should match first 5 items of full result")

                 (is (= (:queried/rows result2)
                        (take 5 (drop 5 full-rows)))
                     "Second page should match items 6-10 of full result")

                 ; Test returned limit/offset match input
                 (is (= (:queried/limit result1) 5)
                     "Returned limit should match input")
                 (is (= (:queried/offset result1) 0)
                     "Returned offset should match input")
                 (is (= (:queried/limit result2) 5)
                     "Returned limit should match input")
                 (is (= (:queried/offset result2) 5)
                     "Returned offset should match input")))
             (done)))))
