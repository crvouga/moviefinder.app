(ns app.media.media-db.inter.pagination-test
  (:require [cljs.test :refer-macros [deftest testing is async]]
            [clojure.core.async :refer [go <!]]
            [app.media.media-db.inter :as media-db]
            [app.media.media-db.backend]
            [app.media.media-db.inter.fixture :as fixture]))

(deftest pagination-test
  (testing "query-result-chan! returns correct paginated subsets"
    (async
     done
     (go
       (doseq [media-db fixture/configs-read-only]
         (let [result-all (<! (media-db/query! media-db {:query/limit 20 :query/offset 0}))
               result-1 (<! (media-db/query! media-db {:query/limit 5 :query/offset 0}))
               result-2 (<! (media-db/query! media-db {:query/limit 5 :query/offset 5}))
               result-3 (<! (media-db/query! media-db {:query/limit 5 :query/offset 10}))
               result-4 (<! (media-db/query! media-db {:query/limit 5 :query/offset 15}))
               result-5 (<! (media-db/query! media-db {:query/limit 5 :query/offset 20}))
               rows-all (:query-result/rows result-all)]

                 ; Test paginated results are subsequences of full result
           (is (= (:query-result/rows result-1) (->> rows-all (drop 0) (take 5)))
               "First page should match first 5 items of full result")

           (is (= (:query-result/rows result-2) (->> rows-all (drop 5) (take 5)))
               "Second page should match items 6-10 of full result")

           (is (= (:query-result/rows result-3) (->> rows-all (drop 10) (take 5)))
               "Third page should match items 11-15 of full result")

           (is (= (:query-result/rows result-4) (->> rows-all (drop 15) (take 5)))
               "Fourth page should match items 16-20 of full result")

           (is (= (:query-result/rows result-5) (->> rows-all (drop 20) (take 5)))
               "Fifth page should match items 21-25 of full result")

           (is (= (:query-result/limit result-1) 5) "Returned limit should match input")
           (is (= (:query-result/offset result-1) 0) "Returned offset should match input")
           (is (= (:query-result/limit result-2) 5) "Returned limit should match input")
           (is (= (:query-result/offset result-2) 5) "Returned offset should match input")
           (is (= (:query-result/limit result-3) 5) "Returned limit should match input")
           (is (= (:query-result/offset result-3) 10) "Returned offset should match input")
           (is (= (:query-result/limit result-4) 5) "Returned limit should match input")
           (is (= (:query-result/offset result-4) 15) "Returned offset should match input")
           (is (= (:query-result/limit result-5) 5) "Returned limit should match input")
           (is (= (:query-result/offset result-5) 20) "Returned offset should match input")))
       (done)))))
