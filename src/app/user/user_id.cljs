(ns app.user.user-id)

(defn gen []
  (str "user:" (random-uuid)))
