(ns core.sql.interface-test
  (:require [core.sql.interface :as sql]
            [core.sql.impl]
            [cljs.test :refer-macros [deftest testing is async]]
            [cljs.core.async :refer [<!] :refer-macros [go]]))

(deftest create-table-test
  (async done
         (testing "Can create a table"
           (go
             (let [db-conn (sql/new! {:sql/impl :sql-impl/pglite})]

          ; Create table
               (<! (-> db-conn
                       (assoc :sql/query "CREATE TABLE IF NOT EXISTS test_table (id INTEGER PRIMARY KEY, name TEXT)")
                       sql/query-chan!))

               (is true "Table creation should not throw error")
               (done))))))

(deftest insert-select-test
  (async done
         (testing "Can insert and select data"
           (go
             (let [db-conn (sql/new! {:sql/impl :sql-impl/pglite})
                   _ (-> db-conn
                         (assoc :sql/query "CREATE TABLE IF NOT EXISTS test_table (id INTEGER PRIMARY KEY, name TEXT)")
                         sql/query-chan!
                         <!)
                   before (-> db-conn
                              (assoc :sql/query "SELECT * FROM test_table WHERE id = 1")
                              sql/query-chan!
                              <!)
                   _ (-> db-conn
                         (assoc :sql/query "INSERT INTO test_table (id, name) VALUES (1, 'test name')")
                         sql/query-chan!
                         <!)
                   after (-> db-conn
                             (assoc :sql/query "SELECT * FROM test_table WHERE id = 1")
                             sql/query-chan!
                             <!)]
               (is (-> before :sql/rows empty?) "Should have no rows before insert")
               (is (-> after :sql/rows count (= 1)) "Should return one row after insert")
               (is (= {:id 1 :name "test name"} (first (:sql/rows after))) "Row should match inserted data"))
             (done)))))
