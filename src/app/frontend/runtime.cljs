(ns app.frontend.runtime
  (:require [clojure.core.async :refer [go <! timeout]]
            [app.frontend.store :as store]))

(defn sleep-eff! [i]
  (let [payload (store/eff-payload i)
        duration (-> payload :sleep/duration (or 0))
        msgs (-> payload :sleep/msgs (or []))]
    (go
      (<! (timeout duration))
      (doseq [msg msgs]
        (store/put! i msg)))))

(store/register-eff!
 :runtime/sleep sleep-eff!)

