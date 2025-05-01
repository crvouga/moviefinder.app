(ns lib.kv.inter-test
  (:require
   [cljs.test :refer-macros [deftest is testing async]]
   [clojure.core.async :refer [<!]]
   [lib.kv.inter :as kv]
   [lib.kv.impl])
  (:require-macros
   [cljs.core.async.macros :refer [go]]))

(deftest kv-operations
  (testing "Basic KV operations"
    (async
     done
     (go
       (let [kv-store (kv/new! {:kv/impl :kv/impl-atom
                                :kv/namespace ::test-namespace})
             test-key "test-key"
             test-value {:data "test-value"}]

         (let [set-result (<! (kv/set! kv-store test-key test-value))]
           (is (= :result/ok (:result/type set-result))
               "Setting a value should return a success result"))

         (let [get-result (<! (kv/get! kv-store test-key))]
           (is (= :result/ok (:result/type get-result))
               "Getting a value should return a success result")
           (is (= test-value (dissoc get-result :result/type))
               "Retrieved value should match what was set"))

         (let [zap-result (<! (kv/zap! kv-store test-key))]
           (is (= :result/ok (:result/type zap-result))
               "Zapping a value should return a success result"))

         (let [get-after-zap (<! (kv/get! kv-store test-key))]
           (is (= :result/ok (:result/type get-after-zap))
               "Getting after zap should still return a success result")
           (println "get-after-zap" get-after-zap)
           (is (nil? (:data get-after-zap))
               "Value should be nil after being zapped"))

         (done))))))

(deftest kv-namespacing
  (testing "KV namespacing"
    (async
     done
     (go
       (let [kv-store-1 (kv/new! {:kv/impl :kv/impl-atom
                                  :kv/namespace ::namespace-1})
             kv-store-2 (kv/new! {:kv/impl :kv/impl-atom
                                  :kv/namespace ::namespace-2})
             test-key "same-key"
             test-value-1 {:data "value-1"}
             test-value-2 {:data "value-2"}]

         ;; Set values in different namespaces
         (<! (kv/set! kv-store-1 test-key test-value-1))
         (<! (kv/set! kv-store-2 test-key test-value-2))

         ;; Verify values are isolated by namespace
         (let [get-result-1 (<! (kv/get! kv-store-1 test-key))
               get-result-2 (<! (kv/get! kv-store-2 test-key))]

           (is (= test-value-1 (dissoc get-result-1 :result/type))
               "Value in namespace 1 should be preserved")
           (is (= test-value-2 (dissoc get-result-2 :result/type))
               "Value in namespace 2 should be preserved")
           (is (not= (dissoc get-result-1 :result/type)
                     (dissoc get-result-2 :result/type))
               "Values in different namespaces should be different"))

         (done))))))


(deftest kv-string-values
  (testing "KV operations with string values"
    (async
     done
     (go
       (let [kv-store (kv/new! {:kv/impl :kv/impl-atom})
             test-key "string-key"
             test-value "This is a string value"]

         ;; Set a string value
         (let [set-result (<! (kv/set! kv-store test-key test-value))]
           (is (= :result/ok (:result/type set-result))
               "Setting a string value should return a success result"))

         ;; Get the string value
         (let [get-result (<! (kv/get! kv-store test-key))]
           (is (= :result/ok (:result/type get-result))
               "Getting a string value should return a success result")
           (is (= test-value (:result/data get-result))
               "Retrieved string value should match what was set"))

         ;; Zap the string value
         (let [zap-result (<! (kv/zap! kv-store test-key))]
           (is (= :result/ok (:result/type zap-result))
               "Zapping a string value should return a success result"))

         ;; Verify string value is gone after zap
         (let [get-after-zap (<! (kv/get! kv-store test-key))]
           (is (= :result/ok (:result/type get-after-zap))
               "Getting after zap should still return a success result")
           (is (nil? (:data get-after-zap))
               "String value should be nil after being zapped"))

         (done))))))
