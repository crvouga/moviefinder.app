(ns moviefinder-app.frontend.toast)

(defn info [message]
  {:toast/variant :toast-variant/info
   :toast/message message
   :toast/duration 3000})