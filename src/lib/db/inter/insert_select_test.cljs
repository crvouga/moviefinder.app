(ns lib.db.inter.insert-select-test
  (:require
   [cljs.test :refer-macros [deftest testing is async]]
   [clojure.core.async :refer [<!] :refer-macros [go]]
   [lib.db.impl]
   [lib.db.inter :as db]
   [lib.db.inter.fixture :refer [create-table-query
                                 insert-query select-query]]))


(deftest insert-select-test
  (async done
         (testing "Can insert and select data"
           (go
             (let [conn (db/new! {:db/impl :db/impl-better-sqlite3})
                   _ (<! (db/query-chan! conn create-table-query))
                   before (<! (db/query-chan! conn select-query))
                   _ (<! (db/query-chan! conn insert-query))
                   after (<! (db/query-chan! conn select-query))]
               (is (-> before :db/rows empty?) "Should have no rows before insert")
               (is (-> after :db/rows count (= 1)) "Should return one row after insert")
               (println "db/rows" (:db/rows after))
               (is (= {:id 1 :name "test name"} (first (:db/rows after))) "Row should match inserted data"))
             (done)))))
