(ns lib.query-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [lib.query :as query]))

(deftest pagination-test
  (testing "to-page-count"
    (is (= (query/to-page-count {:q/limit 20 :q/offset (rand-int 20)} 20) 1))
    (is (= (query/to-page-count {:q/limit 25 :q/offset (rand-int 20)} 20) 2))
    (is (= (query/to-page-count {:q/limit 40 :q/offset (rand-int 20)} 20) 2))
    (is (= (query/to-page-count {:q/limit 41 :q/offset (rand-int 20)} 20) 3)))


  (testing "to-start-page-index"
    (is (= (query/to-start-page-index {:q/limit (rand-int 20) :q/offset 0} 20) 0))
    (is (= (query/to-start-page-index {:q/limit (rand-int 20) :q/offset 20} 20) 1))
    (is (= (query/to-start-page-index {:q/limit (rand-int 20) :q/offset 40} 20) 2))
    (is (= (query/to-start-page-index {:q/limit (rand-int 20) :q/offset 60} 20) 3))
    (is (= (query/to-start-page-index {:q/limit (rand-int 20) :q/offset 80} 20) 4))
    (is (= (query/to-start-page-index {:q/limit (rand-int 20) :q/offset 5} 20) 0))
    (is (= (query/to-start-page-index {:q/limit (rand-int 20) :q/offset 25} 20) 1)))


  (testing "to-end-page-index"
    (is (= (query/to-end-page-index {:q/limit 0 :q/offset 0} 20) 0))
    (is (= (query/to-end-page-index {:q/limit 0 :q/offset 20} 20) 1))
    (is (= (query/to-end-page-index {:q/limit 0 :q/offset 40} 20) 2))
    (is (= (query/to-end-page-index {:q/limit 0 :q/offset 60} 20) 3))
    (is (= (query/to-end-page-index {:q/limit 0 :q/offset 80} 20) 4))
    (is (= (query/to-end-page-index {:q/limit 0 :q/offset 5} 20) 0))
    (is (= (query/to-end-page-index {:q/limit 0 :q/offset 25} 20) 1))
    ;; 
    (is (= (query/to-end-page-index {:q/limit 20 :q/offset 10} 20) 1))
    ;; 
    )


  (testing "to-page-indexes"
    ;; Basic cases
    (is (= (query/to-page-indexes {:q/limit 20 :q/offset 0} 20) [0]))
    (is (= (query/to-page-indexes {:q/limit 40 :q/offset 0} 20) [0 1]))
    (is (= (query/to-page-indexes {:q/limit 40 :q/offset 20} 20) [1 2]))
    (is (= (query/to-page-indexes {:q/limit 40 :q/offset 40} 20) [2 3]))

    ;; ;; Partial page overlaps
    (is (= (query/to-start-page-index {:q/limit 20 :q/offset 10} 20) 0))
    (is (= (query/to-end-page-index {:q/limit 20 :q/offset 10} 20) 1))
    (is (= (query/to-page-indexes {:q/limit 20 :q/offset 10} 20) [0 1]))
    ;; (is (= (query/to-page-indexes {:q/limit 20 :q/offset 20} 20) [1]))
    ;; (is (= (query/to-page-indexes {:q/limit 20 :q/offset 30} 20) [2]))
    ;; (is (= (query/to-page-indexes {:q/limit 20 :q/offset 45} 20) [3]))

    ;; ;; Edge cases
    ;; (is (= (query/to-page-numbers {:q/limit 0 :q/offset 0} 20) [1]))
    ;; (is (= (query/to-page-numbers {:q/limit 1 :q/offset 0} 20) [1]))
    ;; (is (= (query/to-page-numbers {:q/limit 20 :q/offset 19} 20) [1 2]))
    ;; (is (= (query/to-page-numbers {:q/limit 100 :q/offset 0} 20) [1 2 3 4 5]))
    ;; (is (= (query/to-page-numbers {:q/limit 20 :q/offset 999} 20) [50 51]))

    ;; ;; Different page sizes
    ;; (is (= (query/to-page-numbers {:q/limit 10 :q/offset 0} 5) [1 2]))
    ;; (is (= (query/to-page-numbers {:q/limit 10 :q/offset 7} 5) [2]))

    ;; (is (= (query/to-page-numbers {:q/limit 100 :q/offset 80} 20) [5]))
    )

  #_(testing "to-pages"
      (is (= (query/to-pages {:q/limit 20 :q/offset 0} 20)
             [{:page/number 1 :page/limit 20 :page/size 20 :page/offset 0}]))

      (is (= (query/to-pages {:q/limit 20 :q/offset 20} 20)
             [{:page/number 2 :page/limit 20 :page/size 20 :page/offset 0}]))

      (is (= (query/to-pages {:q/limit 60 :q/offset 0} 20)
             [{:page/number 1 :page/limit 20 :page/size 20 :page/offset 0}
              {:page/number 2 :page/limit 20 :page/size 20 :page/offset 0}
              {:page/number 3 :page/limit 20 :page/size 20 :page/offset 0}]))

      (is (= (query/to-pages {:q/limit 10 :q/offset 0} 20)
             [{:page/number 1 :page/limit 10 :page/size 20 :page/offset 0}]))

      (is (= (query/to-pages {:q/limit 10 :q/offset 10} 20)
             [{:page/number 1 :page/limit 10 :page/size 20 :page/offset 10}]))

      (is (= (query/to-pages {:q/limit 20 :q/offset 10} 20)
             [{:page/number 1 :page/limit 20 :page/size 20 :page/offset 10}
              {:page/number 2 :page/limit 20 :page/size 20 :page/offset 0}]))

      ;; Edge cases
      (is (= (query/to-pages {:q/limit 0 :q/offset 0} 20)
             [{:page/number 1 :page/limit 0 :page/size 20 :page/offset 0}]))

      #_(is (= (query/to-pages {:q/limit 25 :q/offset 5} 20)
               [{:page/number 1 :page/limit 20 :page/size 20 :page/offset 5}
                {:page/number 2 :page/limit 5 :page/size 20 :page/offset 0}]))

      (is (= (query/to-pages {:q/limit 20 :q/offset 19} 20)
             [{:page/number 1 :page/limit 20 :page/size 20 :page/offset 19}
              {:page/number 2 :page/limit 20 :page/size 20 :page/offset 0}]))

      #_(is (= (query/to-pages {:q/limit 100 :q/offset 80} 20)
               [{:page/number 5 :page/limit 20 :page/size 20 :page/offset 0}]))))


