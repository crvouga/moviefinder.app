(ns app.media.media-db.inter.pagination-test
  (:require
   [app.media.media-db.backend]
   [app.media.media-db.inter :as media-db]
   [app.media.media-db.inter.fixture :as fixture]
   [cljs.test :refer-macros [deftest testing is async]]
   [clojure.core.async :refer [<! go]]
   [lib.query-result :as query-result]))

(deftest limit-pagination-test
  (testing "limit"
    (async
     done
     (go
       (doseq [media-db fixture/configs-read-only]
         (let [limit 50
               offset 0
               result (<! (media-db/query! media-db {:q/limit limit :q/offset offset}))
               rows (:query-result/rows result)]

           (is (>= (:query-result/total result) limit)
               (str "Media db " (:media-db/impl media-db) " should return at least " limit " rows"))

           (is (= (:query-result/limit result) limit)
               (str "Media db " (:media-db/impl media-db) " should return " limit " rows"))

           (is (= (:query-result/offset result) offset)
               (str "Media db " (:media-db/impl media-db) " should return " offset " offset"))

           (is (= (count rows) limit)
               (str "Media db " (:media-db/impl media-db) " should return " limit " rows"))))

       (done)))))

(deftest pagination-subsets-test
  (testing "returns correct paginated subsets"
    (async
     done
     (go
       (doseq [media-db fixture/configs-read-only]
         (let [limit 50
               offset 0
               result-all (<! (media-db/query! media-db {:q/limit limit :q/offset offset}))
               result-1 (<! (media-db/query! media-db {:q/limit 5 :q/offset 0}))
               result-2 (<! (media-db/query! media-db {:q/limit 5 :q/offset 5}))
               result-3 (<! (media-db/query! media-db {:q/limit 5 :q/offset 10}))
               result-4 (<! (media-db/query! media-db {:q/limit 5 :q/offset 15}))
               result-5 (<! (media-db/query! media-db {:q/limit 5 :q/offset 20}))]

           (is (query-result/subset? result-1 result-all)
               "First page should match first 5 items of full result")

           (is (query-result/subset? result-2 result-all)
               "Second page should match items 6-10 of full result")

           (is (query-result/subset? result-3 result-all)
               "Third page should match items 11-15 of full result")

           (is (query-result/subset? result-4 result-all)
               "Fourth page should match items 16-20 of full result")

           #_(is (query-result/subset? result-5 result-all)
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
           #_(is (= (:query-result/offset result-5) 20) "Returned offset should match input"))))
     (done))))
