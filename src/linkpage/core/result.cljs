(ns linkpage.core.result
  (:require [clojure.spec.alpha :as s]))

(s/def :result/type #{:result/err :result/ok :result/loading :result/not-asked})
(s/def :result/payload any?)
(s/def :result/result (s/tuple :result/type :result/payload))

(comment
  (s/conform :result/result [:result/ok {"Some payload" 122}])
  (s/explain :result/result [:result/err "som error"])
  (s/explain :result/result [:result/unknown "Payload"])
  ;; 
  )


(defn conform [result]
  (if (s/valid? :result/result result)
    result
    [:result/not-asked {}]))

(comment
  (conform nil)
  (conform [:result/ok {:foo :bar}])
  ;; 
  )

(defn payload [result]
  (second result))

(comment
  (payload [:result/ok {:foo :bar}]))

(defn ok? [result]
  (and (s/valid? :result/result result)
       (= (first result) :result/ok)))

(defn loading? [result]
  (and (s/valid? :result/result result)
       (= (first result) :result/loading)))