(ns core.backend.db-conn.interface.insert-select-test
  (:require
   [clojure.core.async :refer [<!] :refer-macros [go]]
   [cljs.test :refer-macros [deftest testing is async]]
   [core.backend.db-conn.impl]
   [core.backend.db-conn.interface :as db-conn]))


(deftest insert-select-test
  (async done
         (testing "Can insert and select data"
           (go
             (let [db-conn (db-conn/new! {:db-conn/impl :db-conn-impl/better-sqlite3})
                   _ (-> db-conn
                         (assoc :db-conn/query "CREATE TABLE IF NOT EXISTS test_table (id INTEGER PRIMARY KEY, name TEXT)")
                         db-conn/query-chan!
                         <!)
                   before (-> db-conn
                              (assoc :db-conn/query "SELECT * FROM test_table WHERE id = 1")
                              db-conn/query-chan!
                              <!)
                   _ (-> db-conn
                         (assoc :db-conn/query "INSERT INTO test_table (id, name) VALUES (1, 'test name')")
                         db-conn/query-chan!
                         <!)
                   after (-> db-conn
                             (assoc :db-conn/query "SELECT * FROM test_table WHERE id = 1")
                             db-conn/query-chan!
                             <!)]
               (is (-> before :db-conn/rows empty?) "Should have no rows before insert")
               (is (-> after :db-conn/rows count (= 1)) "Should return one row after insert")
               (is (= {:id 1 :name "test name"} (first (:db-conn/rows after))) "Row should match inserted data"))
             (done)))))
