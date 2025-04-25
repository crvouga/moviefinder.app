(ns core.backend.db-conn.interface.insert-select-test
  (:require
   [cljs.test :refer-macros [deftest testing is async]]
   [clojure.core.async :refer [<!] :refer-macros [go]]
   [core.backend.db-conn.impl]
   [core.backend.db-conn.interface :as db-conn]
   [core.backend.db-conn.interface.fixture :refer [create-table-query
                                                   insert-query select-query]]))


(deftest insert-select-test
  (async done
         (testing "Can insert and select data"
           (go
             (let [conn (db-conn/new! {:db-conn/impl :db-conn-impl/better-sqlite3})
                   _ (<! (db-conn/query-chan! conn create-table-query))
                   before (<! (db-conn/query-chan! conn select-query))
                   _ (<! (db-conn/query-chan! conn insert-query))
                   after (<! (db-conn/query-chan! conn select-query))]
               (is (-> before :db-conn/rows empty?) "Should have no rows before insert")
               (is (-> after :db-conn/rows count (= 1)) "Should return one row after insert")
               (println "db-conn/rows" (:db-conn/rows after))
               (is (= {:id 1 :name "test name"} (first (:db-conn/rows after))) "Row should match inserted data"))
             (done)))))
