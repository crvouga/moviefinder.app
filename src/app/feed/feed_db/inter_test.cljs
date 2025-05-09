(ns app.feed.feed-db.inter-test
  (:require
   [app.feed.feed-db.impl-kv]
   [app.feed.feed-db.inter :as feed-db]
   [cljs.test :refer-macros [deftest is testing async]]
   [clojure.core.async :refer [<!]]
   [app.feed.feed :as feed]
   [lib.kv.impl]
   [lib.result :as result])
  (:require-macros
   [cljs.core.async.macros :refer [go]]))


(defn new-db []
  (feed-db/init {:feed-db/impl :feed-db/impl-kv
                 :kv/impl :kv/impl-atom}))

(deftest feed-db
  (testing "get and put a feed"
    (async
     done
     (go
       (let [db (new-db)
             test-feed (feed/default)]

         (let [put (<! (feed-db/put! db test-feed))]
           (is (result/ok? put) "Putting a user should return a success result"))

         (let [got (<! (feed-db/get! db (:feed/feed-id test-feed)))]
           (is (result/ok? got) "Getting a feed by ID should return a success result")
           (is (= test-feed (dissoc got :result/type)) "Retrieved feed should match what was put"))
         (done))))))
