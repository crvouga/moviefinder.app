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

(deftest sql-injection-test
  (async done
         (testing "Prevents SQL injection attacks"
           (go
             (let [db-conn (db-conn/new! {:sql/impl :sql-impl/pglite})
                   malicious-input "test'; DROP TABLE test_table; --"
                   _ (-> db-conn
                         (assoc :sql/query {:create-table [:test_table :if-not-exists]
                                            :with-columns [[:id :integer :primary-key]
                                                           [:name :text]]})
                         db-conn/query-chan!
                         <!)
                   _ (-> db-conn
                         (assoc :sql/query {:insert-into :test_table
                                            :columns [:id :name]
                                            :values [[1 malicious-input]]})
                         db-conn/query-chan!
                         <!)
                   result (-> db-conn
                              (assoc :sql/query {:select [:*]
                                                 :from [:test_table]
                                                 :where [:= :name malicious-input]})
                              db-conn/query-chan!
                              <!)]

               ; Table should still exist and contain our data
               (is (= 1 (count (:sql/rows result))) "Should find exactly one row")
               (is (= malicious-input (-> result :sql/rows first :name)) "Malicious input should be stored as-is")

               ; Verify table still exists by querying it
               (let [table-check (-> db-conn
                                     (assoc :sql/query {:select [:*]
                                                        :from [:test_table]})
                                     db-conn/query-chan!
                                     <!)]
                 (is (seq (:sql/rows table-check)) "Table should still exist")))
             (done)))))


(deftest sql-injection-test-2
  (async done
         (testing "Prevents SQL injection in WHERE clause"
           (go
             (let [db-conn (db-conn/new! {:sql/impl :sql-impl/pglite})
                   malicious-where "1=1; DROP TABLE test_table; --"
                   _ (-> db-conn
                         (assoc :sql/query {:create-table [:test_table :if-not-exists]
                                            :with-columns [[:id :integer :primary-key]
                                                           [:name :text]]})
                         db-conn/query-chan!
                         <!)
                   _ (-> db-conn
                         (assoc :sql/query {:insert-into :test_table
                                            :columns [:id :name]
                                            :values [[1 "safe data"]]})
                         db-conn/query-chan!
                         <!)
                   result (-> db-conn
                              (assoc :sql/query {:select [:*]
                                                 :from [:test_table]
                                                 :where [:= :id malicious-where]})
                              db-conn/query-chan!
                              <!)]

               ; Table should still exist and query should return no rows (invalid where clause)
               (is (empty? (:sql/rows result)) "Query with malicious WHERE should return no rows")

               ; Verify table still exists with data intact
               (let [table-check (-> db-conn
                                     (assoc :sql/query {:select [:*]
                                                        :from [:test_table]})
                                     db-conn/query-chan!
                                     <!)]
                 (is (= 1 (count (:sql/rows table-check))) "Table should exist with one row")
                 (is (= "safe data" (-> table-check :sql/rows first :name)) "Original data should be intact")))
             (done)))))
