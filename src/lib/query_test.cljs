(ns lib.query-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [lib.query :as query]))

(deftest pagination
  (testing "to-page-count"
    (is (= (query/to-page-count {:query/limit 20 :query/offset 0} 20) 1))
    (is (= (query/to-page-count {:query/limit 20 :query/offset 20} 20) 2))
    (is (= (query/to-page-count {:query/limit 20 :query/offset 40} 20) 3))
    (is (= (query/to-page-count {:query/limit 20 :query/offset 60} 20) 4)))



  (testing "to-page-numbers"
    ;; Basic cases
    (is (= (query/to-page-numbers {:query/limit 20 :query/offset 0} 20) [1]))
    (is (= (query/to-page-numbers {:query/limit 40 :query/offset 0} 20) [1 2]))
    (is (= (query/to-page-numbers {:query/limit 40 :query/offset 20} 20) [2 3]))
    (is (= (query/to-page-numbers {:query/limit 40 :query/offset 40} 20) [3 4]))

    ;; Partial page overlaps
    (is (= (query/to-page-numbers {:query/limit 20 :query/offset 10} 20) [1 2]))
    (is (= (query/to-page-numbers {:query/limit 20 :query/offset 20} 20) [2]))
    (is (= (query/to-page-numbers {:query/limit 20 :query/offset 30} 20) [2 3]))
    (is (= (query/to-page-numbers {:query/limit 20 :query/offset 45} 20) [3 4]))

    ;; Edge cases
    (is (= (query/to-page-numbers {:query/limit 0 :query/offset 0} 20) [1]))
    (is (= (query/to-page-numbers {:query/limit 1 :query/offset 0} 20) [1]))
    (is (= (query/to-page-numbers {:query/limit 20 :query/offset 19} 20) [1 2]))
    (is (= (query/to-page-numbers {:query/limit 100 :query/offset 0} 20) [1 2 3 4 5]))
    (is (= (query/to-page-numbers {:query/limit 20 :query/offset 999} 20) [50 51]))

    ;; Different page sizes
    (is (= (query/to-page-numbers {:query/limit 10 :query/offset 0} 5) [1 2]))
    (is (= (query/to-page-numbers {:query/limit 10 :query/offset 7} 5) [2 3 4])))

  (testing "to-pages"
    (is (= (query/to-pages {:query/limit 20 :query/offset 0} 20)
           [{:page/number 1 :page/limit 20 :page/size 20 :page/offset 0}]))

    (is (= (query/to-pages {:query/limit 20 :query/offset 20} 20)
           [{:page/number 2 :page/limit 20 :page/size 20 :page/offset 0}]))

    (is (= (query/to-pages {:query/limit 60 :query/offset 0} 20)
           [{:page/number 1 :page/limit 20 :page/size 20 :page/offset 0}
            {:page/number 2 :page/limit 20 :page/size 20 :page/offset 0}
            {:page/number 3 :page/limit 20 :page/size 20 :page/offset 0}]))

    (is (= (query/to-pages {:query/limit 10 :query/offset 0} 20)
           [{:page/number 1 :page/limit 10 :page/size 20 :page/offset 0}]))

    (is (= (query/to-pages {:query/limit 10 :query/offset 10} 20)
           [{:page/number 1 :page/limit 10 :page/size 20 :page/offset 10}]))

    (is (= (query/to-pages {:query/limit 20 :query/offset 10} 20)
           [{:page/number 1 :page/limit 20 :page/size 20 :page/offset 10}
            {:page/number 2 :page/limit 20 :page/size 20 :page/offset 0}]))

    ;; Edge cases
    (is (= (query/to-pages {:query/limit 0 :query/offset 0} 20)
           [{:page/number 1 :page/limit 0 :page/size 20 :page/offset 0}]))

    #_(is (= (query/to-pages {:query/limit 1 :query/offset 999} 20)
             [{:page/number 50 :page/take 1 :page/size 20 :page/drop 19}
              {:page/number 51 :page/take 1 :page/size 20 :page/drop 0}]))

    (is (= (query/to-pages {:query/limit 20 :query/offset 19} 20)
           [{:page/number 1 :page/limit 20 :page/size 20 :page/offset 19}
            {:page/number 2 :page/limit 20 :page/size 20 :page/offset 0}]))

    ;; Very large values
    (is (= (query/to-pages {:query/limit 100 :query/offset 80} 20)
           [{:page/number 5 :page/limit 20 :page/size 20 :page/offset 0}
            {:page/number 6 :page/limit 20 :page/size 20 :page/offset 0}
            {:page/number 7 :page/limit 20 :page/size 20 :page/offset 0}
            {:page/number 8 :page/limit 20 :page/size 20 :page/offset 0}
            {:page/number 9 :page/limit 20 :page/size 20 :page/offset 0}]))))


