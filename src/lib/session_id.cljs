(ns lib.session-id)

(defn gen []
  (str "session:" (random-uuid)))
