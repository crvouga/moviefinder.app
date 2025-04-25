(ns core.promise
  (:require
   [clojure.core.async :refer [chan put!]]))

(defn ->chan [promise]
  (let [c (chan)]
    (-> promise
        (.then #(put! c %))
        (.catch #(put! c {:result/type :result/err
                          :error/message "unknown error"
                          :error/data %})))
    c))
