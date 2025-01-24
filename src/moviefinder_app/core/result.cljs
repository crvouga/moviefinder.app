(ns moviefinder-app.core.result
  (:require [clojure.spec.alpha :as s]))


(s/def :result/type #{:result/err :result/ok :result/loading :result/not-asked})
(s/def :result/payload any?)
(s/def :result/result (s/or :with-payload (s/tuple :result/type :result/payload)
                            :no-payload   (s/tuple :result/type)))

(comment
  (s/conform :result/result [:result/ok {"Some payload" 122}])
  (s/conform :result/result [:result/loading])
  (s/explain :result/result [:result/err "some error"])
  (s/explain :result/result [:result/unknown "Payload"]))

(defn conform [result]
  (if (s/valid? :result/result result)
    result
    [:result/not-asked]))

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

(defn err? [result]
  (and (s/valid? :result/result result)
       (= (first result) :result/err)))

(defn loading? [result]
  (and (s/valid? :result/result result)
       (= (first result) :result/loading)))