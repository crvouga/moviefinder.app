(ns app.media.media-db.inter.query-by-id-test
  (:require
   [app.media.media-db.inter :as media-db]
   [app.media.media-db.inter.fixture :as fixture]
   [app.media.media-id :as media-id]
   [cljs.test :refer-macros [deftest testing is async]]
   [clojure.core.async :refer [<! go]]
   [lib.tmdb-api.shared]))

(def media-id-fight-club (media-id/from-tmdb-id lib.tmdb-api.shared/movie-id-fight-club))
(deftest query-by-id-test
  (testing "Can query media by id"
    (async
     done
     (go
       (doseq [media-db fixture/configs-read-only]
         (let [query {:query/limit 1
                      :query/offset 0
                      :query/where [:= :media/id media-id-fight-club]}
               result (<! (media-db/query! media-db query))
               first-result (first (:query-result/rows result))]

           (is (= media-id-fight-club (-> first-result :media/id))
               (str "Media ID should match for implementation " (:media-db/impl media-db)))))
       (done)))))
