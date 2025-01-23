(ns linkpage.core.result
  (:require [clojure.spec.alpha :as s]))

(s/def :result/type #{:result/err :result/ok :result/loading :result/not-asked})
(s/def :result/payload map?)
(s/def :result/result (s/tuple :result/type :result/payload))

(s/conform :result/result [:result/ok {"Some payload" 122}])
(s/conform :result/result [:result/err {:error "Oops"}])
(s/conform :result/result [:result/unknown "Payload"])

(defn ok? [result]
  (and (vector? result) (= :result/ok (first result))))

(defn loading? [result]
  (and (vector? result) (= :result/loading (first result))))

(defn payload [result]
  (second result))