(ns lib.fs.inter-test
  (:require
   [cljs.test :refer-macros [deftest is testing async]]
   [clojure.core.async :as a]
   [lib.fs.inter :as fs]
   [lib.fs.impl]))


(def test-fs {:fs/impl :fs/node})

(deftest read!-test
  (testing "read! returns content from a file"
    (async
     done
     (a/go
       (a/<! (fs/write-file! test-fs "test-file.txt" "test content for test-file.txt"))
       (let [content (a/<! (fs/read-file! test-fs "test-file.txt"))]
         (is (= "test content for test-file.txt" content))
         (done))))))

(deftest write!-test
  (testing "write! returns true on success"
    (async
     done
     (a/go
       (let [result (a/<! (fs/write-file! test-fs "test-file.txt" "some content"))]
         (is (true? result))
         (done))))))

(deftest exists?-test
  (testing "exists? returns true when file exists"
    (async
     done
     (a/go
       (let [result (a/<! (fs/file-exists? test-fs "test-file.txt"))]
         (is (true? result))
         (done))))))

(deftest delete!-test
  (testing "delete! returns true on success"
    (async
     done
     (a/go
       (let [result (a/<! (fs/delete-file! test-fs "test-file.txt"))]
         (is (true? result))
         (done))))))
