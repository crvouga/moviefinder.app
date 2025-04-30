(ns lib.time)

(defn now! []
  (js/Date.now))