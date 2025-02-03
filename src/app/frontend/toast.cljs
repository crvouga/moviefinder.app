(ns app.frontend.toast)

(defn info [message]
  {:toast/variant :toast-variant/info
   :toast/message message
   :toast/duration 3000})

(defn error [message]
  {:toast/variant :toast-variant/error
   :toast/message message
   :toast/duration 3000})
