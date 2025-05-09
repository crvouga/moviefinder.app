(ns lib.kv.inter-test
  (:require
   [cljs.test :refer-macros [deftest is testing async]]
   [clojure.core.async :refer [<! go]]
   [lib.kv.impl]
   [lib.kv.inter :as kv]
   [lib.result :as result]))

(defn new-kv
  ([]
   (new-kv :test-namespace))
  ([namespace]
   (kv/init {:kv/impl :kv/impl-fs
             :kv/namespace namespace})))

(def k "some-key")
(def v {:kv-test/data "some-value"})

(deftest kv-ops
  (testing "Basic KV operations"
    (async
     done
     (go
       (let [kv (new-kv)]
         (let [set (<! (kv/set! kv k v))]
           (is (result/ok? set) "Setting a value should return a ok result"))

         (let [got (<! (kv/get! kv k))]
           (is (result/ok? got) "Getting a value should return a ok result")
           (is (= v (dissoc got :result/type)) "Retrieved value should match what was set"))

         (let [zapped (<! (kv/zap! kv k))]
           (is (result/ok? zapped) "Zapping a value should return a ok result"))

         (let [got (<! (kv/get! kv k))]
           (is (result/ok? got) "Getting after zap should still return a ok result")
           (is (nil? (:kv-test/data got)) "Value should be nil after being zapped"))

         (done))))))

(deftest kv-namespacing
  (testing "KV namespacing"
    (async
     done
     (go
       (let [kv-1 (new-kv :kv-test/ns-1)
             kv-2 (new-kv :kv-test/ns-2)
             k "same-key"
             v-1 {:kv-test/data "value-1"}
             v-2 {:kv-test/data "value-2"}]

         (<! (kv/set! kv-1 k v-1))
         (<! (kv/set! kv-2 k v-2))

         (let [got-1 (<! (kv/get! kv-1 k))
               got-2 (<! (kv/get! kv-2 k))]

           (is (= v-1 (dissoc got-1 :result/type))
               "Value in namespace 1 should be preserved")
           (is (= v-2 (dissoc got-2 :result/type))
               "Value in namespace 2 should be preserved")
           (is (not= (dissoc got-1 :result/type)
                     (dissoc got-2 :result/type))
               "Values in different namespaces should be different"))

         (done))))))

(deftest kv-string-values
  (testing "KV operations with string values"
    (async
     done
     (go
       (let [kv (new-kv)
             k "string-key"
             v "This is a string value"]
         (let [set (<! (kv/set! kv k v))]
           (is (result/ok? set) "Setting a string value should return a ok result"))

         (let [got (<! (kv/get! kv k))]
           (is (result/ok? got) "Getting a string value should return a ok result")
           (is (= v (:kv/value got)) "Retrieved string value should match what was set"))

         (let [zapped (<! (kv/zap! kv k))]
           (is (result/ok? zapped) "Zapping a string value should return a ok result"))

         (let [got (<! (kv/get! kv k))]
           (is (result/ok? got) "Getting after zap should still return a ok result")
           (is (nil? (:kv/value got)) "String value should be nil after being zapped"))

         (done))))))
