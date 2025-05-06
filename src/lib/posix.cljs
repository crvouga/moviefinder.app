(ns lib.posix)


(defn now! []
  (.getTime (js/Date.)))