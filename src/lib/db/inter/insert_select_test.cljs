(ns lib.db.inter.insert-select-test
  (:require
   [cljs.test :refer-macros [deftest testing is async]]
   [clojure.core.async :refer [<!] :refer-macros [go]]
   [lib.db.impl]
   [lib.db.inter :as db]
   [lib.db.inter.fixture :refer [q-create-table q-insert q-select]]))

(deftest insert-select-test
  (async
   done
   (testing "Can insert and select data"
     (go
       (let [db (db/init {:db/impl :db/impl-better-sqlite3})
             _ (<! (db/query! db q-create-table))
             before (<! (db/query! db q-select))
             _ (<! (db/query! db q-insert))
             after (<! (db/query! db q-select))]

         (is (-> before :db/rows empty?) "Should have no rows before insert")
         (is (-> after :db/rows count (= 1)) "Should return one row after insert")
         (is (= {:id 1 :name "test name"} (first (:db/rows after))) "Row should match inserted data"))
       (done)))))
