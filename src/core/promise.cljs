(ns core.promise
  (:require [cljs.core.async :refer [put! chan]]))

(defn ->chan [promise]
  (let [c (chan)]
    (-> promise
        (.then #(put! c %))
        (.catch #(put! c {:result/type :result/error
                          :error/message (.-message %)
                          :error/data %})))
    c))
