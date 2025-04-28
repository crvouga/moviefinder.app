(ns lib.db.interface.fixture
  (:require
   [lib.db.impl]))

(def create-table-query {:create-table [:test_table :if-not-exists]
                         :with-columns [[:id :integer :primary-key]
                                        [:name :text]]})

(def select-query {:select [:*]
                   :from [:test_table]
                   :where [:= :id 1]})

(def insert-query {:insert-into :test_table
                   :columns [:id :name]
                   :values [[1 "test name"]]})
