(ns lib.db.interface.honeysql-test
  (:require
   [clojure.core.async :refer [<!] :refer-macros [go]]
   [cljs.test :refer-macros [deftest testing is async]]
   [lib.db.impl]
   [lib.db.interface :as db-conn]
   [lib.db.interface.fixture :refer [create-table-query select-query insert-query]]))


(deftest insert-select-honeysql-test
  (async done
         (testing "Can insert and select data using HoneySQL"
           (go
             (let [conn (db-conn/new! {:db/impl :db/impl-better-sqlite3})
                   _ (<! (db-conn/query-chan! conn create-table-query))
                   before (<! (db-conn/query-chan! conn select-query))
                   _ (<! (db-conn/query-chan! conn insert-query))
                   after (<! (db-conn/query-chan! conn select-query))]

               (is (-> before :db/rows empty?) "Should have no rows before insert")

               (is (-> after :db/rows count (= 1)) "Should return one row after insert")

               (is (= {:id 1 :name "test name"} (first (:db/rows after))) "Row should match inserted data"))

             (done)))))

