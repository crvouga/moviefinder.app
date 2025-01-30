(ns core.db-conn.interface.honeysql-test
  (:require [core.db-conn.interface :as db-conn]
            [core.db-conn.impl]
            [cljs.test :refer-macros [deftest testing is async]]
            [cljs.core.async :refer [<!] :refer-macros [go]]))

(deftest insert-select-honeysql-test
  (async done
         (testing "Can insert and select data using HoneySQL"
           (go
             (let [db-conn (db-conn/new! {:db-conn/impl :db-conn-impl/pglite})
                   _ (-> db-conn
                         (assoc :db-conn/query {:create-table [:test_table :if-not-exists]
                                                :with-columns [[:id :integer :primary-key]
                                                               [:name :text]]})
                         db-conn/query-chan!
                         <!)
                   before (-> db-conn
                              (assoc :db-conn/query {:select [:*]
                                                     :from [:test_table]
                                                     :where [:= :id 1]})
                              db-conn/query-chan!
                              <!)
                   _ (-> db-conn
                         (assoc :db-conn/query {:insert-into :test_table
                                                :columns [:id :name]
                                                :values [[1 "test name"]]})
                         db-conn/query-chan!
                         <!)
                   after (-> db-conn
                             (assoc :db-conn/query {:select [:*]
                                                    :from [:test_table]
                                                    :where [:= :id 1]})
                             db-conn/query-chan!
                             <!)]
               (is (-> before :db-conn/rows empty?) "Should have no rows before insert")
               (is (-> after :db-conn/rows count (= 1)) "Should return one row after insert")
               (is (= {:id 1 :name "test name"} (first (:db-conn/rows after))) "Row should match inserted data"))
             (done)))))

