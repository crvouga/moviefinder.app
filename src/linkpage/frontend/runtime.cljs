(ns linkpage.frontend.runtime
  (:require [clojure.core.async :refer [go <! timeout]]
            [linkpage.frontend.store :as store]))

(store/reg-eff!
 :runtime/sleep
 (fn [i]
   (let [payload (store/eff-payload i)
         duration (-> payload :sleep/duration (or 0))
         msgs (-> payload :sleep/msgs (or []))]
     (go
       (<! (timeout duration))
       (doseq [msg msgs]
         (store/put! i msg))))))

