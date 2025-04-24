(ns core.db-conn.interface.sql-injection-test
  (:require
   [cljs.core.async :refer [<!] :refer-macros [go]]
   [cljs.test :refer-macros [deftest testing is async]]
   [core.db-conn.impl]
   [core.db-conn.interface :as db-conn]))

(deftest sql-injection-test
  (async done
         (testing "Prevents SQL injection attacks"
           (go
             (let [db-conn (db-conn/new! {:db-conn/impl :db-conn-impl/pglite})
                   malicious-input "test'; DROP TABLE test_table; --"
                   _ (-> db-conn
                         (assoc :db-conn/query {:create-table [:test_table :if-not-exists]
                                                :with-columns [[:id :integer :primary-key]
                                                               [:name :text]]})
                         db-conn/query-chan!
                         <!)
                   _ (-> db-conn
                         (assoc :db-conn/query {:insert-into :test_table
                                                :columns [:id :name]
                                                :values [[1 malicious-input]]})
                         db-conn/query-chan!
                         <!)
                   result (-> db-conn
                              (assoc :db-conn/query {:select [:*]
                                                     :from [:test_table]
                                                     :where [:= :name malicious-input]})
                              db-conn/query-chan!
                              <!)]

               ; Table should still exist and contain our data
               (is (= 1 (count (:db-conn/rows result))) "Should find exactly one row")
               (is (= malicious-input (-> result :db-conn/rows first :name)) "Malicious input should be stored as-is")

               ; Verify table still exists by querying it
               (let [table-check (-> db-conn
                                     (assoc :db-conn/query {:select [:*]
                                                            :from [:test_table]})
                                     db-conn/query-chan!
                                     <!)]
                 (is (seq (:db-conn/rows table-check)) "Table should still exist")))
             (done)))))


(deftest sql-injection-test-2
  (async done
         (testing "Prevents SQL injection in WHERE clause"
           (go
             (let [db-conn (db-conn/new! {:db-conn/impl :db-conn-impl/pglite})
                   malicious-where "1=1; DROP TABLE test_table; --"
                   _ (-> db-conn
                         (assoc :db-conn/query {:create-table [:test_table :if-not-exists]
                                                :with-columns [[:id :integer :primary-key]
                                                               [:name :text]]})
                         db-conn/query-chan!
                         <!)
                   _ (-> db-conn
                         (assoc :db-conn/query {:insert-into :test_table
                                                :columns [:id :name]
                                                :values [[1 "safe data"]]})
                         db-conn/query-chan!
                         <!)
                   result (-> db-conn
                              (assoc :db-conn/query {:select [:*]
                                                     :from [:test_table]
                                                     :where [:= :id malicious-where]})
                              db-conn/query-chan!
                              <!)]

               ; Table should still exist and query should return no rows (invalid where clause)
               (is (empty? (:db-conn/rows result)) "Query with malicious WHERE should return no rows")

               ; Verify table still exists with data intact
               (let [table-check (-> db-conn
                                     (assoc :db-conn/query {:select [:*]
                                                            :from [:test_table]})
                                     db-conn/query-chan!
                                     <!)]
                 (is (= 1 (count (:db-conn/rows table-check))) "Table should exist with one row")
                 (is (= "safe data" (-> table-check :db-conn/rows first :name)) "Original data should be intact")))
             (done)))))
