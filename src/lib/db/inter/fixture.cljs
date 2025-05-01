(ns lib.db.inter.fixture
  (:require
   [lib.db.impl]))

(def q-create-table
  {:create-table [:test_table :if-not-exists]
   :with-columns [[:id :integer :primary-key]
                  [:name :text]]})

(def q-select
  {:select [:*]
   :from [:test_table]
   :where [:= :id 1]})

(def q-insert
  {:insert-into :test_table
   :columns [:id :name]
   :values [[1 "test name"]]})
