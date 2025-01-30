(ns core.db-conn.interface-test
  (:require [core.db-conn.interface :as db-conn]
            [core.db-conn.impl]
            [cljs.test :refer-macros [deftest testing is async]]
            [cljs.core.async :refer [<!] :refer-macros [go]]))

(deftest create-table-test
  (async done
         (testing "Can create a table"
           (go
             (let [db-conn (db-conn/new! {:sql/impl :sql-impl/pglite})]

          ; Create table
               (<! (-> db-conn
                       (assoc :sql/query "CREATE TABLE IF NOT EXISTS test_table (id INTEGER PRIMARY KEY, name TEXT)")
                       db-conn/query-chan!))

               (is true "Table creation should not throw error")
               (done))))))

(deftest insert-select-test
  (async done
         (testing "Can insert and select data"
           (go
             (let [db-conn (db-conn/new! {:sql/impl :sql-impl/pglite})
                   _ (-> db-conn
                         (assoc :sql/query "CREATE TABLE IF NOT EXISTS test_table (id INTEGER PRIMARY KEY, name TEXT)")
                         db-conn/query-chan!
                         <!)
                   before (-> db-conn
                              (assoc :sql/query "SELECT * FROM test_table WHERE id = 1")
                              db-conn/query-chan!
                              <!)
                   _ (-> db-conn
                         (assoc :sql/query "INSERT INTO test_table (id, name) VALUES (1, 'test name')")
                         db-conn/query-chan!
                         <!)
                   after (-> db-conn
                             (assoc :sql/query "SELECT * FROM test_table WHERE id = 1")
                             db-conn/query-chan!
                             <!)]
               (is (-> before :sql/rows empty?) "Should have no rows before insert")
               (is (-> after :sql/rows count (= 1)) "Should return one row after insert")
               (is (= {:id 1 :name "test name"} (first (:sql/rows after))) "Row should match inserted data"))
             (done)))))

(deftest insert-select-honeysql-test
  (async done
         (testing "Can insert and select data using HoneySQL"
           (go
             (let [db-conn (db-conn/new! {:sql/impl :sql-impl/pglite})
                   _ (-> db-conn
                         (assoc :sql/query {:create-table [:test_table :if-not-exists]
                                            :with-columns [[:id :integer :primary-key]
                                                           [:name :text]]})
                         db-conn/query-chan!
                         <!)
                   before (-> db-conn
                              (assoc :sql/query {:select [:*]
                                                 :from [:test_table]
                                                 :where [:= :id 1]})
                              db-conn/query-chan!
                              <!)
                   _ (-> db-conn
                         (assoc :sql/query {:insert-into :test_table
                                            :columns [:id :name]
                                            :values [[1 "test name"]]})
                         db-conn/query-chan!
                         <!)
                   after (-> db-conn
                             (assoc :sql/query {:select [:*]
                                                :from [:test_table]
                                                :where [:= :id 1]})
                             db-conn/query-chan!
                             <!)]
               (is (-> before :sql/rows empty?) "Should have no rows before insert")
               (is (-> after :sql/rows count (= 1)) "Should return one row after insert")
               (is (= {:id 1 :name "test name"} (first (:sql/rows after))) "Row should match inserted data"))
             (done)))))
