(ns core.promise
  (:require
   [cljs.core.async :refer [chan put!]]))

(defn ->chan [promise]
  (let [c (chan)]
    (-> promise
        (.then #(put! c %))
        (.catch #(put! c {:result/type :result/error
                          :error/message "unknown error"
                          :error/data %})))
    c))
